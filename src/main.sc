require: modules.js
    type = scriptEs6
    name = modules
require: slotfilling/slotFilling.sc
  module = sys.zb-common

patterns:
    $entryService = ((записать|записаться|оформить|оформи|прохож|пройти|сделать).*(то|тех\s*обслужив|техобсл|то-?\s*\d|то\s*\d)|подошло время то|то надо|то-?1|перво[е|го] то|тех обслуживание)
   

init:
    bind("preProcess", function() {
        try {
            if (modules.preprocessText($request.query || "")) {
                $request.query = modules.preprocessText($request.query || "");
            }
            if (!$session.fio) $session = {
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
        script:
            $jsapi.startSession()
        q: * $entryService || toState = "/a_service_ask_missing"

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
            const params =  modules.extractParams($parseTree.text || "");
            log(JSON.stringify({sex: "sex", params}))
            if (params?.fio && !$session.fio) $session.fio = params?.fio;
            if (params?.phone && !$session.phone) $session.phone = params?.phone;
            if (params?.car && !$session.car) $session.car = params?.car;
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
            $session = { fio: '', phone: '', car: '' };
        go!: /Start