theme: /
    state: q_sm_howareyou
        a: Отлично! А у вас?
        q: * $sm_positive || toState = "/sm_react_positive"
        q: * $sm_negative || toState = "/sm_react_negative"
        q: * || toState = "/sm_react_positive"

    state: q_sm_name
        a: Меня зовут Вася.
        q: * $sm_introduce || toState = "/sm_name_done"
        q: * || toState = "/Start"

    state: q_sm_user_intro
        q: * $sm_introduce || toState = "/sm_name_done"
        q: * || toState = "/sm_name_done"
