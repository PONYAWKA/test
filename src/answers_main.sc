theme: /
    state: a_service_confirm
        scriptEs6:
            $temp.outFio = $session && $session.fio ? $session.fio : '—';
            $temp.outPhone = $session && $session.phone ? $session.phone : '—';
            $temp.outCar = $session && $session.car ? $session.car : '—';
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

    state: a_service_ask_missing
        a: Давайте оформим заявку на ТО. Пожалуйста, укажите недостающие данные.
        a: Недостаёт: {{ $temp.missingText }}
        a: {{ $temp.promptText }}
        q: * || toState = "/q_service_collect_or_confirm"

    state: a_hours
        a: Работаем ежедневно с 09:00 до 21:00 без выходных.
        go!: /Start

    state: a_address
        a: Наш адрес: г. Москва, ул. Примерная, д. 10. Есть парковка для клиентов.
        go!: /Start

    state: a_services
        a: Выполняем ТО, диагностику, ремонт, шиномонтаж, замену масел и расходников. Для записи на ТО напишите: "записаться на ТО".
        go!: /Start
