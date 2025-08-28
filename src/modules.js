/* eslint-disable */
// ES6 скриптовый модуль для JAICP

import BRAND_SYNONYMS from './brands.json' with { type: 'json' };

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

function onlyDigits(text) {
    if (!text) return '';
    return String(text).replace(/\D+/g, '');
}

function normalizePhone(raw) {
    const digits = onlyDigits(raw);
    if (!digits) return '';
    let d = digits;
    if (d.length === 11 && (d.startsWith('7') || d.startsWith('8'))) {
        d = '7' + d.slice(1);
    } else if (d.length === 10) {
        d = '7' + d;
    }
    if (d.length !== 11) return '';
    return d;
}

function titleCaseRu(text) {
    if (!text) return '';
    return text
        .toLowerCase()
        .split(/\s+/)
        .map(part => part ? part.charAt(0).toUpperCase() + part.slice(1) : '')
        .join(' ')
        .trim();
}

function normalizeFio(raw) {
    if (!raw) return '';
    // Убираем лишнее, оставляем буквы/дефисы/пробелы
    const cleaned = raw.replace(/[^A-Za-zА-Яа-яЁё\-\s]/g, ' ').replace(/\s{2,}/g, ' ').trim();
    // Ограничим до 3 слов (ФИО)
    const parts = cleaned.split(/\s+/).slice(0, 3);
    return titleCaseRu(parts.join(' '));
}

function isLikelyName(raw) {
    if (!raw) return false;
    const lower = String(raw).toLowerCase();
    // Отсечём явные признаки намерений/служебных слов
    const badNeedles = [
        'запис', 'оформ', 'хочу', 'надо', 'пройти', 'сдела',
        'тех', 'обслужив', 'техобсл', 'заявк', 'перво', 'то-1', 'то1', ' то ', ' на то '
    ];
    if (badNeedles.some(n => lower.includes(n))) return false;
    // Нормализуем к ФИО-формату и проверим число слов
    const normalized = normalizeFio(raw);
    if (!normalized) return false;
    const tokens = normalized.split(/\s+/).filter(Boolean);
    if (tokens.length === 0 || tokens.length > 3) return false;
    // Слова не должны быть слишком короткими
    if (tokens.some(t => t.length < 2)) return false;
    return true;
}

function normalizeCarBrand(raw) {
    if (!raw) return '';
    const lc = raw.toLowerCase().replace(/[^a-zа-яё0-9\s\-]/gi, ' ').replace(/\s{2,}/g, ' ').trim();
    // Поиск по словарю синонимов
    for (const canonical in BRAND_SYNONYMS) {
        const variants = BRAND_SYNONYMS[canonical];
        if (!Array.isArray(variants)) continue;
        for (const v of variants) {
            const needle = String(v).toLowerCase();
            // Ищем точное вхождение по границам слова
            const re = new RegExp(`(^|\\b)${needle}(\\b|$)`, 'i');
            if (re.test(lc)) return canonical;
        }
    }
    // Если не нашли по словарю, не делаем агрессивных предположений
    return '';
}

function extractPhone(text) {
    if (!text) return '';
    const m = String(text).match(/(?:(?:\+?7|8)[\s\-]?\(?\d{3}\)?[\s\-]?\d{3}[\s\-]?\d{2}[\s\-]?\d{2})|\b\d{10,11}\b/);
    return normalizePhone(m ? m[0] : '');
}

function extractName(text) {
    if (!text) return '';
    // Пытаемся поймать конструкции: "меня зовут X", "моё имя X", "я X" (коротко)
    const patterns = [
        /меня\s+зовут\s+([^.,!\n\r]{2,60})/i,
        /мо[её]\s+имя\s+([^.,!\n\r]{2,60})/i,
        /я\s+([^.,!\n\r]{2,60})/i,
    ];
    for (const re of patterns) {
        const m = text.match(re);
        if (m && m[1]) {
            const candidate = m[1];
            if (!isLikelyName(candidate)) continue;
            return normalizeFio(candidate);
        }
    }
    return '';
}

function extractCar(text) {
    if (!text) return '';
    return normalizeCarBrand(text);
}


function parseInfo(input) {
  const carBrands = [
    "шкода", "skoda", "audi", "bmw", "мерседес", "mercedes",
    "toyota", "honda", "kia", "ford", "nissan", "mazda",
    "hyundai", "volkswagen", "volvo", "tesla"
  ];

  const result = { name: null, surname: null, phone: null, car: null };
  let text = String(input);

  // 1) Телефон (берём последовательность цифр, допускаем +, пробелы, скобки, дефисы)
  const phoneMatch = text.match(/(\+?\d[\d\-\s()]{6,}\d)/);
  if (phoneMatch) {
    result.phone = phoneMatch[0].replace(/[^\d+]/g, "");
    text = text.replace(phoneMatch[0], " ");
  }

  // Вспомогалка для экранирования брендов в regex
  function escapeRegExp(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  // 2) Поиск марки авто — Unicode-aware границы
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
      result.car = matchedBrand.toLowerCase();
      // удаляем из текста найденную марку (только её, чтобы не сломать соседние слова)
      const rem = new RegExp(escapeRegExp(matchedBrand), "iu");
      text = text.replace(rem, " ");
      break;
    }
  }

  // 3) Очищаем и разбиваем на слова — вытаскиваем имя и фамилию
  const cleaned = text
    .replace(/меня зовут/giu, " ")
    .replace(/[^\p{L}\s]/gu, " ") // оставляем только буквы и пробелы (Unicode-aware)
    .trim()
    .split(/\s+/)
    .filter(Boolean);

  if (cleaned.length > 0) result.name = cleaned[0];
  if (cleaned.length > 1) result.surname = cleaned[1];

  return result;
}

function extractParams(text) {
    const { name,surname, phone, car} = parseInfo(text)

    return { phone, fio: `${name} ${surname}`, car };
}

export default {
    preprocessText,
    normalizePhone,
    normalizeFio,
    normalizeCarBrand,
    extractPhone,
    extractName,
    extractCar,
    extractParams,
};


