require: modules.js
    type = scriptEs6
    name = modules
require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
patterns:
    $thanks = (спасибо [и] [тебе|вам] [большое]|благодар*|спс|супер|супир|ура|отлично|молод*|умни*|пасиб*)  
    $greet = (прив[еe]т|здравствуй|добрый [день|вечер|утро]|хай|йо)
    $help = (помог[и|ите]|что (ты|вы) умеешь|помощь|меню|как (записаться|оформить) [то|то-|техобслуживание])
    $bye = (пока|досвидан|до встречи|увидим[с|ся]|bye|всего доброго)
    $entryService = ((записать|записаться|оформить|оформи|прохож|пройти|сделать).*(то|тех\s*обслужив|техобсл|то-?\s*\d|то\s*\d)|подошло время то|то надо|то-?1|перво[е|го] то|тех обслуживание)
    $hours = (режим|часы|график|когда (работа[е|ю]те|открыты)|до скольки)
    $address = (адрес|где (находите[с|сь]|вы)|как (доехать|добраться))
    $services = (услуг[аи]|что (дела[е|ю]те|обслуживаете)|ремонт|шиномонтаж|диагностик[аи])
  
init:
    bind("preProcess", function() {
        try {
            if (modules.preprocessText($request.text || "")) {
                $request.text = modules.preprocessText($request.text || "");
            }
            if (!$session.service) $session.service = {
                fio: "",
                phone: "",
                car: "",
                };
        } catch (e) {}
    });
  
theme: /
    
    state: Start
        q!: $regex</start>
        a: Здравствуйте! Я бот автосервиса. Помогу записаться на техобслуживание и отвечу на вопросы.
        a: Чтобы оформить запись, напишите, например: "записаться на ТО" или укажите сразу ФИО/телефон/марку авто.
        q: * $greet || a = "Здравствуйте! Чем могу помочь?"
        q: * $help || a = "Я могу: записать на ТО, рассказать про часы работы, адрес и услуги. Напишите: 'записаться на ТО'."
        q: * $thanks || a = "Всегда пожалуйста!"
        q: * $bye || a = "До свидания! Обращайтесь."
        q: * $hours || toState = "/a_hours"
        q: * $address || toState = "/a_address"
        q: * $services || toState = "/a_services"
        q: * $entryService || toState = "/a_service_collect_or_confirm"

    state: q_service_book_main
        q!: * $entryService
        go!: /a_service_collect_or_confirm

    state: a_service_collect_or_confirm
        scriptEs6:
            const params = modules.extractParams($request.text || "");
            console.log(params)
            if (params.fio && !$session.service.fio) $session.service.fio = params.fio;
            if (params.phone && !$session.service.phone) $session.service.phone = params.phone;
            if (params.car && !$session.service.car) $session.service.car = params.car;
            const known = [!!$session.service?.fio, !!$session.service?.phone, !!$session.service?.car].filter(Boolean).length;
            if (known >= 2) {
                $reactions.transition("/a_service_confirm");
            } else {
                $reactions.transition("/a_service_ask_missing");
            }
        event: noMatch || toState = "Start"
        
    state: a_service_ask_missing
        scriptEs6:
            var missing = [];
            if (!$session.service?.fio) missing.push('ФИО');
            if (!$session.service?.phone) missing.push('номер телефона');
            if (!$session.service?.car) missing.push('марка автомобиля');
            if (missing.length === 0) {
                $reactions.transition("/a_service_confirm");
            }
            $temp.missingText = missing.join(', ');
            // Подсказка по первому недостающему параметру
            var prompt = '';
            if (missing[0] === 'номер телефона') prompt = 'Укажите номер телефона в формате +7XXXXXXXXXX или 8XXXXXXXXXX.';
            if (missing[0] === 'ФИО') prompt = 'Укажите, пожалуйста, ваше ФИО. Например: "Меня зовут Иванов Иван".';
            if (missing[0] === 'марка автомобиля') prompt = 'Укажите марку автомобиля (например: Шкода, Toyota, ВАЗ).';
            $temp.promptText = prompt;
        a: Давайте оформим заявку на ТО. Пожалуйста, укажите недостающие данные.
        a: Недостаёт: {{ $temp.missingText }}
        a: {{ $temp.promptText }}
        go!: /q_service_catch_any

    state: q_service_catch_any
        q!: *
        scriptEs6:
            const params = modules.extractParams($request.text || "");
            if (params.fio) $session.service.fio = params.fio;
            if (params.phone) $session.service.phone = params.phone;
            if (params.car) $session.service.car = params.car;
            const known = [!!$session.service?.fio, !!$session.service?.phone, !!$session.service?.car].filter(Boolean).length;
            if (known >= 2) {
                $reactions.transition("/a_service_confirm");
            } else {
                $reactions.transition("/a_service_ask_missing");
            }
        
    state: a_service_confirm
        scriptEs6:
            // Подготовка безопасных значений
            $temp.outFio = $session.service?.fio || '—';
            $temp.outPhone = $session.service?.phone || '—';
            $temp.outCar = $session.service?.car || '—';
        a: Оформляю заявку на техобслуживание на следующие данные:
        a: {{ $temp.outFio }}
        a: номер телефона {{ $temp.outPhone }}
        a: автомобиль {{ $temp.outCar }}.
        a: Наш сотрудник свяжется с вами и уточнит время.
        scriptEs6:
            // Очистим сессию для следующей заявки
            $session.service = { fio: '', phone: '', car: '' };
        go!: /Меню

    state: a_hours
        a: Работаем ежедневно с 09:00 до 21:00 без выходных.
        go!: /Меню

    state: a_address
        a: Наш адрес: г. Москва, ул. Примерная, д. 10. Есть парковка для клиентов.
        go!: /Меню

    state: a_services
        a: Выполняем ТО, диагностику, ремонт, шиномонтаж, замену масел и расходников. Для записи на ТО напишите: "записаться на ТО".
        go!: /Меню
