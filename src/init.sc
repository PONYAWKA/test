require: modules.js
    type = scriptEs6
    name = modules
require: converters.js
    type = scriptEs6
    name = converters
require: slotfilling/slotFilling.sc
    module = sys.zb-common

init:
    bind("preProcess", function() {
        try {
            if (modules.preprocessText($parseTree.text || "")) {
                $parseTree.text = modules.preprocessText($parseTree.text || "");
            }
            if (!$session || typeof $session !== 'object') $session = {};
            if (!$session.fio) $session.fio = "";
            if (!$session.phone) $session.phone = "";
            if (!$session.car) $session.car = "";
        } catch (e) {}
    });
