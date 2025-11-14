
const PRICING_BASE = (() => {
    const env = import.meta.env.VITE_PRICING_API as string | undefined;
    if (env && env.trim()) return env.replace(/\/+$/, "");
    return `${window.location.origin}/api`;
})();

console.log("PRICING_BASE:", PRICING_BASE);

async function fetchJson<T>(url: string, init?: Omit<RequestInit, "body"> & { body?: any }): Promise<T> {
    const res = await fetch(url, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        body:
            typeof init?.body === "string" || init?.body == null
                ? init?.body
                : JSON.stringify(init?.body),

    });
    if (!res.ok) {
        const txt =  await res.text().catch(() => "");
        const err: any = new Error(`HTTP ${res.status} ${res.statusText}${txt ? `: ${txt}` : ""}`);
        err.status = res.status;
        err.body = txt;
        throw err;
    }

    const ct = res.headers.get("content-type") || "";
    console.log("Response content-type:", ct);
    console.log("Response status:", res.status);

    if (ct.includes("application/json")) {
        const data = await res.json();
        console.log("Parsed JSON data:", data);
        return data as T;
    }
    const text = await res.text();
    throw new Error(`Expected JSON but got "${ct}". Body: ${text.slice(0, 200)}`);
}

export async function getPrice(ean: string, storeId?: number) {
    const qs = storeId != null ? `?storeId=${encodeURIComponent(String(storeId))}` : "";
    return fetchJson<{ ean: string; price: number; currency: string; storeId?: number}>(
        `${PRICING_BASE}/pricing/${encodeURIComponent(ean)}${qs}`
    );
}

export async function upsertPrice(args: { ean: string; price: number; currency: string; storeId?: number }) {
    return fetchJson<{ ean: string; price: number; currency: string; storeId?: number}>(
        `${PRICING_BASE}/pricing/upsert`,
        { method: "POST", body: args }
    );
}

export async function getTotal(userId: string, storeId?: number) {
    console.log("getTotal called with userId", userId);
    return fetchJson<{ total: number; currency: string; missing?: string[] }>(
        `${PRICING_BASE}/pricing/total`,
        {
            method: "POST",
            body: { userId, storeId }
    });
}