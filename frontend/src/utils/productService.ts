import { getPrice, upsertPrice } from '../components/pricing/pricingClient';

export type Product = {
    ean: string;
    name?: string;
    product_name?: string;
    displayName?: string;

    brand?: string;
    price?: number;

    imageUrl?: string;
    imageThumbUrl?: string;

    image_url?: string,
    image_thumb_url?: string;
    image_small_url?: string;
    image_front_small_url?: string;
    image_front_thumb_url?: string;
    image_front_url?: string;
}

const API_BASE = (() => {
    const env = import.meta.env.VITE_API_BASE_URL as string | undefined;
    if (env && env.trim()) return env.replace(/\/+$/, "");

    if (typeof window !== "undefined" && window.location?.origin) {
        return `${window.location.origin}/api`;
    }
    return "http://localhost:8080/api";
})();

export async function fetchProduct(ean: string): Promise<Product> {
    const prod = await fetchJson<Product>(
        `${API_BASE}/catalog/products/${encodeURIComponent(ean)}`
    );

    if (!prod) {
        throw new Error(`No product found for EAN ${ean}`);
    }

    return normalizeProduct(prod);
}

function normalizeProduct(prod: Product): Product {
    const pickedImageUrl =
        prod.imageThumbUrl ||
        prod.imageUrl ||

        prod.image_thumb_url ||
        prod.image_url ||
        prod.image_small_url ||
        prod.image_front_small_url ||
        prod.image_front_thumb_url ||
        prod.image_front_url ||
        undefined;

    const displayName = prod.displayName || prod.product_name || prod.name || prod.ean;

    return {
        ...prod,
        name: displayName,
        imageUrl: pickedImageUrl || undefined,
    }

}

export async function ensureProductPrice(product: Product): Promise<number> {
    let priceInfo = await getPrice(product.ean).catch(() => null);
    if (!priceInfo) {
        const input = window.prompt(`Enter price (NOK) for ${product.name}:`, "0");
        if (!input) throw new Error("Price is required to add the item");
        const p = Number(input);
        if (!Number.isFinite(p) || p < 0) throw new Error("Invalid price");
        priceInfo = await upsertPrice({ ean: product.ean, price: p, currency: "NOK"});
    }

    return priceInfo.price;
}

export async function fetchJson<T = any>(
    url: string,
    init?: RequestInit & { body?: any}
): Promise <T> {
    const res = await fetch(url, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            ...(init?.headers || {}),
        },
        body:
            typeof init?.body === "string" || init?.body == null
                ? init?.body
                : JSON.stringify(init?.body),
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        console.error("HTTP error", res.status, res.statusText, "at", url, "body", text);
        const err: any = new Error(`HTTP ${res.statusText}${text ? `: ${text}` : ""}`);
        err.status = res.status;
        throw err;
    }

    const ct = res.headers.get("content-type") || "";
    return ct.includes("application/json") ? ((await res.json()) as T) : (undefined as T);
}