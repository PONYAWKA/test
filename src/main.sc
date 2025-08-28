require: modules.js
    type = scriptEs6
    name = modules
require: slotfilling/slotFilling.sc
  module = sys.zb-common

patterns:
    $greet = (прив[еe]т|здравствуй|добрый [день|вечер|утро]|хай)
    $help = (помощь|что (ты|вы) умеешь|меню|как (записаться|оформить) (то|техобслуживание))
    $thanks = (спасибо|благодар[ю|им]|спс)
    $bye = (пока|до свидан|до встречи|всего доброго)
    $hours = (режим|часы|график|когда (работа[е|ю]те|открыты)|до скольки)
    $address = (адрес|где (находите[с|сь]|вы)|как (доехать|добраться))
    $services = (услуг[аи]|что (дела[е|ю]те|обслуживаете)|ремонт|шиномонтаж|диагностик[аи])
    $entryService = ((записать|записаться|оформить|оформи|прохож|пройти|сделать).*(то|тех\s*обслужив|техобсл|то-?\s*\d|то\s*\d)|подошло время то|то надо|то-?1|перво[е|го] то|тех обслуживание)
  

init:
    bind("preProcess", function() {
        try {
            if (modules.preprocessText($request.text || "")) {
                $request.text = modules.preprocessText($request.text || "");
            }
            if (!$session || typeof $session !== 'object') $session = {};
            if (!$session.fio) $session.fio = "";
            if (!$session.phone) $session.phone = "";
            if (!$session.car) $session.car = "";
        } catch (e) {}
    });
  
theme: /
    
    state: Start
        q!: $regex</start>
        a: Здравствуйте! Я бот автосервиса. Помогу записаться на техобслуживание и отвечу на вопросы.
        a: Чтобы оформить запись, напишите, например: "записаться на ТО" или укажите сразу ФИО/телефон/марку авто.
        script:
            $jsapi.startSession()
        q: * $greet || a = "Здравствуйте! Чем могу помочь?"
        q: * $help || a = "Я могу: записать на ТО, рассказать про часы работы, адрес и услуги. Напишите: 'записаться на ТО'."
        q: * $thanks || a = "Всегда пожалуйста!"
        q: * $bye || a = "До свидания! Обращайтесь."
        q: * $hours || toState = "/a_hours"
        q: * $address || toState = "/a_address"
        q: * $services || toState = "/a_services"
        q: * $entryService || toState = "/a_service_collect_or_confirm"

    state: a_service_ask_missing
        scriptEs6:
            var missing = [];
            if (!$session?.fio) missing.push('ФИО');
            if (!$session?.phone) missing.push('номер телефона');
            if (!$session?.car) missing.push('марка автомобиля');
            if (missing.length === 0) {
                $reactions.transition("/a_service_confirm");
            }
            $temp.missingText = missing.join(', ');
            var prompt = '';
            if (missing[0] === 'номер телефона') prompt = 'Укажите номер телефона в формате +7XXXXXXXXXX или 8XXXXXXXXXX.';
            if (missing[0] === 'ФИО') prompt = 'Укажите, пожалуйста, ваше ФИО. Например: "Меня зовут Иванов Иван".';
            if (missing[0] === 'марка автомобиля') prompt = 'Укажите марку автомобиля (например: Шкода, Toyota, ВАЗ).';
            $temp.promptText = prompt;
        a: Давайте оформим заявку на ТО. Пожалуйста, укажите недостающие данные.
        a: Недостаёт: {{ $temp.missingText }}
        a: {{ $temp.promptText }}
        q: * || toState = "/a_service_collect_or_confirm"

    state: a_service_collect_or_confirm
        scriptEs6:
            const params = modules.extractParams($request.text || "");
            if (params?.fio) $session.fio = params.fio;
            if (params?.phone) $session.phone = params.phone;
            if (params?.car) $session.car = params.car;
            // Мягкая валидация телефона: если есть цифры, но нормализация не прошла — подскажем формат
            const rawDigits = String($request.text || "").replace(/\D+/g, "");
            if (rawDigits.length >= 5 && !params?.phone && !$session.phone) {
                $reactions.answer("Похоже, номер в неверном формате. Укажите телефон в виде +7XXXXXXXXXX или 8XXXXXXXXXX (10–11 цифр).");
            }
            const known = [!!$session?.fio, !!$session?.phone, !!$session?.car].filter(Boolean).length;
            if (known >= 2) {
                $reactions.transition("/a_service_confirm");
            } else {
                $reactions.transition("/a_service_ask_missing");
            }
    state: a_service_confirm
        scriptEs6:
            $temp.outFio = $session?.fio || '—';
            $temp.outPhone = $session?.phone || '—';
            $temp.outCar = $session?.car || '—';
        a: Оформляю заявку на техобслуживание на следующие данные:
        a: {{ $temp.outFio }}
        a: номер телефона {{ $temp.outPhone }}
        a: автомобиль {{ $temp.outCar }}.
        a: Наш сотрудник свяжется с вами и уточнит время.
        scriptEs6:
            $session.fio = '';
            $session.phone = '';
            $session.car = '';
        go!: /Start

    state: a_hours
        a: Работаем ежедневно с 09:00 до 21:00 без выходных.
        go!: /Start

    state: a_address
        a: Наш адрес: г. Москва, ул. Примерная, д. 10. Есть парковка для клиентов.
        go!: /Start

    state: a_services
        a: Выполняем ТО, диагностику, ремонт, шиномонтаж, замену масел и расходников. Для записи на ТО напишите: "записаться на ТО".
        go!: /Start