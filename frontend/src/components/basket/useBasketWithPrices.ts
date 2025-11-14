import { useEffect, useState } from "react";
import { useBasketStream } from "./useBasketStream";
import { getPrice } from "../pricing/pricingClient";


export type BasketItemWithPrice = {
    ean: string;
    name: string;
    qty: number;
    imageUrl: string;
    price?: number;
};

export type BasketWithPrices = {
    basketId: number;
    items: BasketItemWithPrice[];
    total?: number;
};

export function useBasketWithPrices(): BasketWithPrices & { refetch: () => void} {
    const basket = useBasketStream();
    const [itemsWithPrices, setItemsWithPrices] = useState<BasketItemWithPrice[]>([]);
    const [refreshKey, setRefreshKey] = useState(0);
    const refetch = () => setRefreshKey(prev => prev + 1);

    useEffect(() => {
        const fetchPrices = async () => {
            const itemsWithPriceData = await Promise.all(
                basket.items.map(async (item) => {

                    if(item.ean.startsWith('MANUAL')) {
                        return { ...item, price: item.price };
                    }

                    try {
                        const priceInfo = await getPrice(item.ean);
                        return { ...item, price: priceInfo.price };
                    } catch (e) {
                        console.error(`Failed to get price for ${item.ean}`, e);
                        return { ...item, price: undefined };
                    }
                })
            );
            setItemsWithPrices(itemsWithPriceData);
        };

        if (basket.items.length > 0) {
            fetchPrices();
        } else {
            setItemsWithPrices([]);
        }
    }, [basket.items, refreshKey]);

    const calculatedTotal = itemsWithPrices.reduce((sum, item) => {
        if(item.price != null) {
            return sum + (item.price * item.qty);
        }
        return sum;
    }, 0)

    return {
        basketId: basket.basketId,
        items: itemsWithPrices,
        total: calculatedTotal,
        refetch,
    }
}

