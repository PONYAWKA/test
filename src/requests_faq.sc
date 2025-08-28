theme: /
	state: q_faq_cost
		go!: /a_faq_cost

	state: q_faq_need_schedule
		go!: /a_faq_need_schedule

	state: q_faq_complaint
		go!: /a_faq_complaint

	state: q_faq_why
		go!: /a_faq_why

	state: q_faq_noise
		a: Давайте запишу вас на техническое обслуживание? Так мастера проверят звук и диагностику.
		q: * $entryService || toState = "/q_service_ask_missing"
		go!: /Start

	state: q_faq_discounts
		go!: /a_faq_discounts

	state: q_faq_duration
		go!: /a_faq_duration

	state: q_faq_scope
		go!: /a_faq_scope
