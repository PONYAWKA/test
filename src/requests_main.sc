theme: /
    state: Start
        q!: $regex</start>
        script:
            $jsapi.startSession()
        q: $regex</start> || toState = "/a_start_greeting"
        q: * $greet || toState = "/a_sm_greet"
        q: * $sm_howareyou || toState = "/q_sm_howareyou"
        q: * $sm_name || toState = "/q_sm_name"
        q: * $help || a = "Я могу: записать на ТО, рассказать про часы работы, адрес и услуги. Напишите: 'записаться на ТО'."
        q: * $thanks || a = "Всегда пожалуйста!"
        q: * $bye || a = "До свидания! Обращайтесь."
        q: * $hours || toState = "/a_hours"
        q: * $address || toState = "/a_address"
        q: * $services || toState = "/a_services"
        q: * $faq_cost || toState = "/q_faq_cost"
        q: * $faq_need_schedule || toState = "/q_faq_need_schedule"
        q: * $faq_complaint || toState = "/q_faq_complaint"
        q: * $faq_why || toState = "/q_faq_why"
        q: * $faq_noise || toState = "/q_faq_noise"
        q: * $faq_discounts || toState = "/q_faq_discounts"
        q: * $faq_duration || toState = "/q_faq_duration"
        q: * $faq_scope || toState = "/q_faq_scope"
        q: * $entryService || toState = "/q_service_ask_missing"

    state: q_service_ask_missing
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
        go!: /a_service_ask_missing

    state: q_service_collect_or_confirm
        scriptEs6:
            const params = modules.extractParams($parseTree.text || "");
            if (params?.fio && !$session.fio) $session.fio = params.fio;
            if (params?.phone && !$session.phone) $session.phone = params.phone;
            if (params?.car && !$session.car) $session.car = params.car;
            const rawDigits = String($parseTree.text || "").replace(/\D+/g, "");
            if (rawDigits.length >= 5 && params?.phone?.length > 4 && !$session.phone) {
                $reactions.answer("Похоже, номер в неверном формате. Укажите телефон в виде +7XXXXXXXXXX или 8XXXXXXXXXX (10–11 цифр).");
                $reactions.transition("/q_service_ask_missing");
                return;
            }
            const known = [!!$session?.fio, !!$session?.phone, !!$session?.car].filter(Boolean).length;
            if (known >= 2) {
                $reactions.transition("/a_service_confirm");
            } else {
                $reactions.transition("/q_service_ask_missing");
            }
