theme: /
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
