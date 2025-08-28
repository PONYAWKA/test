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
            return normalizeFio(m[1]);
        }
    }
    return '';
}

function extractCar(text) {
    if (!text) return '';
    return normalizeCarBrand(text);
}

function extractParams(text) {
    const cleaned = preprocessText(text);
    const phone = extractPhone(cleaned) ?? "";
    const fio = extractName(cleaned) ?? "";
    const car = extractCar(cleaned) ?? "";
    
    return { cleaned, phone, fio, car };
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


