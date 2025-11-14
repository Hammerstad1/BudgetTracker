export type BasketItemDto = { ean: string; name: string; qty: number; imageUrl: string };
export type BasketDto = {
    basketId: number;
    items: Array<{
        ean: string;
        name: string;
        qty: number;
        imageUrl: string;
        price?: number;
    }>
    total?: number;

};

const BASE = (() => {
    const env = import.meta.env.VITE_BASKET_API as string | undefined;
    if (env && env.trim()) return env.replace(/\/+$/, "");
    return `${window.location.origin}/api`;
})();

export function openBasketStream(
    onData: (data: BasketDto) => void,
): EventSource {
    const url = `${BASE}/basket/stream`;
    const handler = (e: MessageEvent) => onData(JSON.parse(e.data));

    const es = new EventSource(url);
    es.addEventListener("snapshot", handler);
    es.addEventListener("update", handler);
    es.onmessage = handler;

    es.onerror = (err) => console.warn("SSE error:", err, "url:", url);
    return es;
}


export async function addItem(req: { ean: string; name: string; qty?: number; imageUrl?: string | null; price?: number | null }) {
    const safeReq = {
        ean: req.ean,
        name: req.name,
        qty: req.qty ?? 1,
        imageUrl: (req.imageUrl && req.imageUrl.trim()) || "/placeholder.png",
        price: req.price ?? null,

    };
    const res = await fetch(`${BASE}/basket/items`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(safeReq),
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`AddItem failed: ${res.status} ${text}`);
    }
}

export async function clearBasket(){
    const res = await fetch(`${BASE}/basket/items`, { method: "DELETE" });
    if (!res.ok) {
        throw new Error(`Clear basket failed: ${res.status}`);
    }
}

export async function removeItem(ean: string, qty = 1) {
    const res = await fetch(`${BASE}/basket/items/${encodeURIComponent(ean)}?qty=${qty}`, {
        method: "DELETE",
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
    throw new Error(`Remove item failed: ${res.status} ${text}`);
    }
}