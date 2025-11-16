export class BarcodeErrorMapper {

    static getProductErrorMessage(error: unknown, ean: string): string {

        if(error instanceof Error) {
            const msg = error.message;

            if (
                msg.includes('"Failed to retrieve product"') ||
                msg.includes('"Failed to retrieve product"')
            ) {
                return `We couldn't find any product with this barcode (${ean})`
            }

            const lower = msg.toLowerCase();
            if (
                lower.includes("network") ||
                lower.includes("failed to fetch") ||
                lower.includes("timeout")
            ) {
                return "There was a problem contacting the server. Please check your connection and try again";
            }

        }

        return "Something went wrong while adding the product. Please try again, or add the product manually";
    }

    static getScannerErrorMessage(rawError: string | null): string | null {
        if (!rawError) return null;

        if (rawError.includes("NotAllowedError")) {
            return "We need acces to your camera to scan barcodes.";
        }

        if (rawError.includes("NotFoundError")) {
            return "No camera was found on this device.";
        }

        if (rawError.toLowerCase().includes("Not supported")) {
            return "Barcode scanning is not supported in this browser.";
        }

        return "We couldn't start the camera. Please try again or add an item manually";
    }

}

