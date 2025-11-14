import { useEffect, useState } from "react";
import { openBasketStream } from "./basketApi";
import type { BasketDto } from "./basketApi";
import { getTotal } from "../pricing/pricingClient";

export function useBasketStream() {
    const [basket, setBasket] = useState<BasketDto>({ basketId: 0, items: [], total: 0 });

    useEffect(() => {
        const es = openBasketStream(async (basketData) => {
            setBasket(basketData);

            try {
                console.log("About to call getTotal...")
                const result= await getTotal("u1");
                console.log("Full response from getTotal:", result.total);
                console.log("Total from pricing services:", result.total);
                setBasket(prev => ({ ...prev, total: result.total }));
            } catch (e) {
                console.error("Failed to fetch total:", e);
            }
        })
        return () => es.close();
    }, []);

    return basket;
}