require: modules.js
    type = scriptEs6
    name = modules
require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
patterns:
    $thanks = (спасибо [и] [тебе|вам] [большое]|благодар*|спс|супер|супир|ура|отлично|молод*|умни*|пасиб*)  
  
init:
    bind("preProcess", function() {
        try {
            if (modules.preprocessText($request.text || "")) {
                $request.text = modules.preprocessText($request.text || "");
            }
            if (!$session.service) $session.service = {
                fio: "",
                phone: "",
                };
        } catch (e) {}
    });
  
theme: /
    
    state: Start
        q!: $regex</start>
        a: Здравствуйте! Я бот автосервиса. Помогу записаться на техобслуживание и отвечу на вопросы.
        q: * $thanks || toState = "/a_service_ask_missing"

    state: q_service_book_main
        q!:*{(записаться|запиши|оформить)}*
        go!: /a_service_collect_or_confirm

    state: a_service_collect_or_confirm
        scriptEs6:
            const params = modules.extractParams($request.text || "");
            console.log(params)
            if (params.fio && !$session.service.fio) $session.service.fio = params.fio;
            if (params.phone && !$session.service.phone) $session.service.phone = params.phone;
            const known = [!!$session.service?.fio, !!$session.service?.phone].filter(Boolean).length;
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
        a: Давайте оформим заявку на ТО. Пожалуйста, укажите недостающие данные.
        a: Недостаёт: {{ $temp.missingText }}
        
    state: a_service_confirm
        a: Оформляю заявку на техобслуживание на следующие данные:
        a: {{ $session.service.fio }}
        a: номер телефона {{ $session.service.phone }}
        a: автомобиль {{ $session.service.car }}.
        a: Наш сотрудник свяжется с вами и уточнит время.
        scriptEs6:
            // Очистим сессию для следующей заявки
            $session.service = {};
        go!: /Меню
