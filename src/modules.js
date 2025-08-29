/* eslint-disable */
// ES6 скриптовый модуль для JAICP

function clampLength(text, maxLen) {
    if (!text || typeof text !== 'string') return '';
    return text.length > maxLen ? text.slice(0, maxLen) : text;
}

function preprocessText(text) {
    if (!text) return '';
    let cleaned = text
        .replace(/[\u0000-\u001F\u007F]/g, ' ')
        .replace(/[\t\r\n]+/g, ' ')
        .replace(/[_*`~^<>\[\]{}|\\]/g, ' ')
        .replace(/\s{2,}/g, ' ')
        .trim();
    cleaned = clampLength(cleaned, 250);
    return cleaned;
}

function parseInfo(input) {
    const carBrands = [
        "шкода", "skoda", "audi", "bmw", "мерседес", "mercedes",
        "toyota", "honda", "kia", "ford", "nissan", "mazda",
        "hyundai", "volkswagen", "volvo", "tesla",
        "lada", "lada niva", "lada samara", "lada granta", "lada kalina", "lada priora", "lada 4x4"
    ];

    const result = { name: null, surname: null, phone: null, car: null };
    let text = String(input);

    const phoneMatch = text.match(/(\+?\d[\d\-\s()]{6,}\d)/);
    if (phoneMatch) {
        result.phone = phoneMatch[0].replace(/[^\d+]/g, "");
        text = text.replace(phoneMatch[0], " ");
    }

    function escapeRegExp(s) {
        return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    }

    for (const brand of carBrands) {
        let matchedBrand = null;
        let re = null;

        try {

            const pattern = '(^|[^\\p{L}])(' + escapeRegExp(brand) + ')(?=$|[^\\p{L}])';
            re = new RegExp(pattern, "iu");
            const m = text.match(re);
            if (m) matchedBrand = m[2]; // группа с самим брендом
        } catch (err) {

            re = new RegExp("\\b" + escapeRegExp(brand) + "\\b", "i");
            const m2 = text.match(re);
            if (m2) matchedBrand = m2[0];
        }

        if (matchedBrand) {
            result.car = matchedBrand.toLowerCase()
            const rem = new RegExp(escapeRegExp(matchedBrand), "iu");
            text = text.replace(rem, " ");
            break;
        }
    }

    const cleaned = text
        .replace(/меня зовут/giu, " ")
        .replace(/[^\p{L}\s]/gu, " ")
        .trim()
        .split(/\s+/)
        .filter(Boolean);

    if (cleaned.length > 0) result.name = cleaned[0];
    if (cleaned.length > 1) result.surname = cleaned[1];

    return result;
}

function extractParamsConverter(text) {
    const { name, surname, phone, car } = parseInfo(text)

    return { phone, fio: `${name} ${surname}`, car };
}

export default {
    extractParamsConverter,
    preprocessText
};


