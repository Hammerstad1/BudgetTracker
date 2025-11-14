import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import {removeItem, addItem} from "./basket/basketApi";
import { useBasketWithPrices } from "./basket/useBasketWithPrices";
import { useBudgetAlerts } from "./pricing/useBudgetAlerts";
import { BudgetSettings } from "./pricing/BudgetSettings";

type ToastData = { name: string, price: number | null; imageUrl: string | null; ean: string};

export default function Home() {
    const navigate = useNavigate();
    const location = useLocation() as { state?: { toast?: ToastData; total?: number } };
    const [toast, setToast] = useState<ToastData | null>(location.state?.toast ?? null);
    const [showSettings, setShowSettings] = useState(false);
    const [showAddProduct, setShowAddProduct] = useState(false);
    const [dismissedAlert, setDismissedAlert] = useState(false);

    const [productName, setProductName] = useState("");
    const [productAmount, setProductAmount] = useState("1");
    const [productPrice, setProductPrice] = useState("");

    const { refetch: refetchBasket, ...basket } = useBasketWithPrices();
    const budgetAlert = useBudgetAlerts(basket.total ?? 0);
    const shouldShowAlert = !!budgetAlert && !dismissedAlert;


    useEffect(() => {
        if (budgetAlert) {
            setShowSettings(false);
        }
    }, [budgetAlert?.timestamp]);


    useEffect(() => {
        if (location.state?.toast) {
            setToast(location.state?.toast);
            navigate(".", {replace: true, state: null})
        }
    }, []);

    useEffect(() => {
        if (!toast) return;
        const id = setTimeout(() => setToast(null), 3000);
        return () => clearTimeout(id);
    }, [toast]);

    useEffect(() => {
        console.log('budgetAlert state:', budgetAlert);
    }, [budgetAlert]);

    useEffect(() => {
        console.log('Basket items:', basket.items);
        basket.items.forEach(item => {
            console.log(`Item: ${item.name}, Price: ${item.price}, EAN: ${item.ean}`);
        });
    }, [basket.items]);

    useEffect(() => {
        console.log('budgetAlert state:', budgetAlert);
    }, [budgetAlert]);

    const handleAddProduct = async () => {
        if (!productName.trim()) {
            alert("Please enter a product name");
            return;
        }

        const amount = parseInt(productAmount);
        if (isNaN(amount) || amount  < 1) {
            alert("Please enter a product amount");
            return;
        }

        const price = productPrice.trim() ? parseFloat(productPrice) : null;
        if (productPrice.trim() && (isNaN(price!) || price! < 0)) {
            alert("Please enter a product price");
            return;
        }

        try {
            const manuelEAN = `MANUAL-${Date.now()}-${Math.floor(Math.random() * 1000)}`;

            await addItem({
                ean: manuelEAN,
                name: productName.trim(),
                qty: amount,
                imageUrl: null,
                price: price,
            });

            await refetchBasket();


            setProductName("");
            setProductAmount("1");
            setProductPrice("");
            setShowAddProduct(false);

            setToast({
                name: productName.trim(),
                price: price,
                imageUrl: null,
                ean: manuelEAN,
            });
        } catch (error) {
            console.error("Error adding product", error);
            alert("Could not add product. Please try again.");
        }
    };

    return (
        <div className={"app-bg"}>
            <div className="phone-frame">
                <header className="header">
                    <h1>Grocery store list</h1>
                </header>

                {shouldShowAlert && (
                    <div className="modal-overlay" onClick={() => setDismissedAlert(true)}>
                        <div className="budget-alert"
                             style={{
                                 position: "relative",
                                 maxWidth: '250px',
                                 marginTop: '-500px',
                        }}
                        >
                            <button
                                onClick={() => setDismissedAlert(true)}
                                style={{
                                    position: "absolute",
                                    top: 8,
                                    right: 8,
                                    background: 'transparent',
                                    border: 'none',
                                    fontSize: '24px',
                                    cursor: "pointer",
                                    opacity: 0.6,
                                    lineHeight: '1',
                                    padding: '0 4px',
                                }}
                                aria-label="Dismiss alert"
                                title="Dismiss"
                            >
                                x
                            </button>
                            <strong>Budget warning!</strong>
                            <div style={{ marginTop: 4, fontSize: "14px"}}>
                                {"You've spent "}
                                <strong>{(budgetAlert?.currentTotal ?? 0).toFixed(2)} NOK </strong> {" "}
                                ({(budgetAlert?.thresholdPercentage ?? 0).toFixed(0)}% of your {" "}
                                {(budgetAlert?.budget ?? 0).toFixed(2)} NOK budget)
                            </div>
                        </div>
                    </div>
                )}

                <section className="list-panel" style={{ position: "relative" }}>
                    <div style={{
                        display: "flex",
                        flexDirection: "column",
                        gap: 12,
                        width: "95%",
                    }} >
                        {basket.items.map((it =>
                                <div
                                    key={it.ean}
                                    className="list-item"
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: 6,
                                        padding: "6px 10px",
                                    }}
                                >
                                    {it.imageUrl && (
                                        <img src={it.imageUrl} width={22} height={22} alt="" style={{ borderRadius: 4, objectFit: "cover" }} />
                                    )}
                                    <span style={{ flex: 1 }}></span>
                                    <span>{it.name}</span>
                                    <strong style={{ marginLeft: 4 }}>x{it.qty}</strong>

                                    {it.price != null && (
                                        <span style={{ marginLeft: 8, fontSize: "0.9em", color: "#666" }}>
                                            {Number(it.price).toFixed(2)} NOK
                                        </span>
                                    )}

                                    <button
                                        aria-label={`Remove ${it.name}`}
                                        title="Remove"
                                        onClick={async() => {
                                            try {
                                                await removeItem(it.ean);
                                                await refetchBasket();
                                            } catch (e) {
                                                console.error(e);
                                                alert("Could not remove item.");
                                            }
                                        }}
                                        style={{
                                            marginLeft: 6,
                                            display: "inline-flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            width: 26,
                                            height: 26,
                                            borderRadius: 6,
                                            border: "1px solid #ffb3b3",
                                            background: "#f9f9f9",
                                            cursor: "pointer",
                                        }}
                                    >
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M3 6h18"/>
                                        <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                                        <path d="M10 11v6M14 11v6"/>
                                        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                                    </svg>
                                </button>
                             </div>
                        ))}
                    </div>
                </section>

                <div
                    style={{
                        display: "flex",
                        gap: "12px",
                        justifyContent: "flex-end",
                        padding: "0 16px",
                        marginTop: "8px",
                    }}
                >
                    <button
                        onClick={() => setShowSettings(!showSettings)}
                        style = {{
                            background: "#3b8f5a",
                            color: "white",
                            border: "none",
                            borderRadius: 8,
                            padding: "8px 12px",
                            cursor: "pointer",
                            fontSize: "14px",
                            fontWeight: 600,
                        }}
                    >
                        Budget
                    </button>

                    <button
                        onClick={() => setShowAddProduct(true)}
                        style = {{
                            background: "#3b8f5a",
                            color: "white",
                            border: "none",
                            borderRadius: 8,
                            padding: "8px 12px",
                            cursor: "pointer",
                            fontSize: "14px",
                            fontWeight: 600,
                        }}
                    >
                        Add a product
                    </button>
                </div>

                <div className="price-row">
                    <div className="price-label">Total price: </div>
                    <div className="price-box">
                        {basket.total != null ? Number(basket.total).toFixed(2) : '0.00' } NOK
                    </div>
                </div>


                <div className="cta">
                    <button id="scanBtn" onClick={() => navigate("/scan")}>
                        <div className="barcode"></div>
                        <div>
                            <span>Scan barcode</span>
                        </div>
                    </button>
                </div>
            </div>

            {showAddProduct && (
                <div className="modal-overlay" onClick={() => setShowAddProduct(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2 style={{ marginTop: 0, color: "#3b8f5a"}}>Add Product</h2>

                        <div style={{ display: "flex", flexDirection: "column", gap: "16px"}}>
                            <div>
                                <label style={{ display: "block", marginBottom: "6px", fontWeight: 600, fontSize: "14px"}}>
                                    Product Name
                                </label>
                                <input
                                    type="text"
                                    value={productName}
                                    onChange={(e) => setProductName(e.target.value)}
                                    placeholder="Product Name"
                                    style={{
                                        width: "100%",
                                        paddingLeft: "10px",
                                        border: "1px solid #e9d5c3",
                                        borderRadius: "8px",
                                        fontSize: "14px",
                                        boxSizing: "border-box",
                                    }}
                                />
                            </div>

                            <div>
                                <label style={{ display: "block", marginBottom: "6px", fontWeight: 600, fontSize: "14px"}}>
                                    Amount
                                </label>
                                <input
                                    type="number"
                                    value={productAmount}
                                    onChange={(e) => setProductAmount(e.target.value)}
                                    style={{
                                        width: "100%",
                                        paddingLeft: "10px",
                                        border: "1px solid #e9d5c3",
                                        borderRadius: "8px",
                                        fontSize: "14px",
                                        boxSizing: "border-box",
                                    }}
                                />
                            </div>

                            <div>
                                <label style={{ display: "block", marginBottom: "6px", fontWeight: 600, fontSize: "14px"}}>
                                    Price (NOK)
                                </label>
                                <input
                                    type="number"
                                    value={productPrice}
                                    onChange={(e) => setProductPrice(e.target.value)}
                                    style={{
                                        width: "100%",
                                        paddingLeft: "10px",
                                        border: "1px solid #e9d5c3",
                                        borderRadius: "8px",
                                        fontSize: "14px",
                                    }}
                                />
                            </div>

                            <div style={{ display: "flex", gap: "8px", marginTop: "8px" }}>
                                <button
                                    onClick={handleAddProduct}
                                    style={{
                                        flex: 1,
                                        padding: "12px",
                                        background: "#3b8f5a",
                                        color: "white",
                                        border: "none",
                                        borderRadius: "12px",
                                        cursor: "pointer",
                                        fontWeight: 600,
                                        fontSize: "14px",
                                    }}
                                >
                                    Add to list
                                </button>
                                <button
                                    onClick={() => {
                                        setShowAddProduct(false);
                                        setProductName("");
                                        setProductAmount("1");
                                        setProductPrice("");
                                    }}
                                    style={{
                                        flex: 1,
                                        padding: "12px",
                                        background: "#666",
                                        color: "white",
                                        border: "none",
                                        borderRadius: "12px",
                                        cursor: "pointer",
                                        fontWeight: 600,
                                        fontSize: "14px",

                                    }}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

                {showSettings && (
                    <div className="modal-overlay" onClick={() => setShowSettings(false)}>
                        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                            <BudgetSettings />
                            <button
                                onClick={() => setShowSettings(false)}
                                style={{
                                    marginTop: 16,
                                    width: '100%',
                                    padding: 12,
                                    background: '#666',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: 12,
                                    cursor: 'pointer',
                                }}
                            >
                                Close { }
                            </button>
                        </div>
                    </div>
                )}
        </div>
    )

}