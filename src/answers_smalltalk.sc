theme: /
    state: a_start_greeting
        a: Здравствуйте! Я бот автосервиса. Помогу записаться на техобслуживание и отвечу на вопросы.
        a: Чтобы оформить запись, напишите, например: "записаться на ТО".
        go!: /Start

    state: a_sm_greet
        a: Здравствуйте!
        a: Как у вас дела?
        go!: /q_sm_howareyou

    state: a_sm_help
        a: Я могу: записать на ТО, рассказать про часы работы, адрес и услуги. Напишите: 'записаться на ТО'.
        go!: /Start

    state: a_sm_thanks
        a: Всегда пожалуйста!
        go!: /Start

    state: a_sm_bye
        a: До свидания! Обращайтесь.
        go!: /Start

    state: sm_react_positive
        a: Я рад. Теперь давайте поговорим про автомобили.
        go!: /Start

    state: sm_react_negative
        a: Жаль. Возможно, я смогу помочь. Задайте мне любой вопрос про наш автосервис.
        go!: /Start

    state: sm_name_done
        a: Приятно познакомиться!
        go!: /Start
