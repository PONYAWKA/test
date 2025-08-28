/* eslint-disable */
// Конвертеры для именованных паттернов JAICP DSL

function onlyDigits(text) {
    if (!text) return '';
    return String(text).replace(/\D+/g, '');
}

function phoneConverter(raw) {
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

export default {
    phoneConverter,
};
