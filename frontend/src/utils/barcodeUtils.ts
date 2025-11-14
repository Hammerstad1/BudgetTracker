import { BarcodeFormat, DecodeHintType } from '@zxing/library'

export const BARCODE_HINTS = new Map<DecodeHintType, unknown>([
    [DecodeHintType.POSSIBLE_FORMATS, [
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
    ]],
    [DecodeHintType.TRY_HARDER, true],
]);

export function normalizeRetailCode(text: string, format: BarcodeFormat): string | null {
    const raw = text.trim().replace(/\s+/g,"");
    const digits = raw.replace(/\D/g, "");
    if (!digits) return null;

    if (format === BarcodeFormat.EAN_13) {
        return isValidGTIN(digits, 13) ? digits : null;
    }

    if (format === BarcodeFormat.EAN_8) {
        return isValidGTIN(digits, 8) ? digits : null;
    }

    if (format === BarcodeFormat.UPC_A) {
        const ean13 = `0${digits}`;
        return isValidGTIN(ean13, 13) ? ean13 : null;
    }

    if (format === BarcodeFormat.UPC_E) {
        const upcA = expandUPCEtoUPCA(digits);
        if (!upcA) return null;
        const ean13 = `0${upcA}`;
        return isValidGTIN(ean13, 13) ? ean13 : null;
    }
    return null;
}

function isValidGTIN(code: string, len : 8 | 12 | 13 | 14): boolean {
    if (code.length !== len || !/^\d+$/.test(code)) return false;
    let sum = 0;

    for (let i = 0; i < len - 1; i++) {
        const n = Number(code[len - 2 - i]);
        sum += n * (i % 2 === 0 ? 3 : 1);
    }
    const check = (10 - (sum % 10)) % 10;
    return check === Number(code[len - 1]);
}


function expandUPCEtoUPCA(upce: string): string | null {
    if (!/^\d{8}$/.test(upce)) return null;
    const ns = upce [0];
    const mfg = upce.slice(1, 6);
    const check = upce[7];
    if (ns !== '0' && ns !== '1') return null;

    const last = mfg[4];
    let upcA = "";
    if (last === '0' || last === '1' || last === '2') {
        upcA = `${ns}${mfg.slice(0,2)}${last}0000${mfg.slice(2,4)}${check}`;
    } else if (last === '3'){
        upcA = `${ns}${mfg.slice(0,3)}00000${mfg.slice(3,4)}${check}`;
    } else if (last === '4'){
        upcA = `${ns}${mfg.slice(0,4)}00000${mfg[3]}${check}`;
    } else {
        upcA = `${ns}${mfg.slice(0,5)}0000${last}${check}`;
    }
    return /^\d{12}$/.test(upcA) ? upcA : null;
}