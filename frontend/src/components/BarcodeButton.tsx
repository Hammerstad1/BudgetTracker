import { useState } from "react";
import { getTotal } from './pricing/pricingClient';
import { useBarcodeScanner } from '../utils/useBarcodeScanner';
import { fetchProduct, ensureProductPrice } from '../utils/productService';
import type { Product } from '../utils/productService';
import { useNavigate } from 'react-router-dom';
import { addItem } from './basket/basketApi';
import { USER_ID, TOAST_DURATION } from '../constants';


export default function BarcodeButton() {
    const navigate = useNavigate();
    const [product, setProduct] = useState<Product | null>(null);
    const [total, setTotal] = useState<number | null>(null);
    const [showToast, setShowToast] = useState(false);
    const [scanError, setScanError] = useState<string | null>(null);

    const handleScan = async (ean: string) => {

        setScanError(null);

        try {
            const prod = await fetchProduct(ean)
            prod.price = await ensureProductPrice(prod);
            setProduct(prod);

            await addItem({
                ean: prod.ean,
                name: prod.displayName || prod.product_name || prod.name || prod.ean,
                qty: 1,
                imageUrl: prod.imageUrl ?? '/placeholder.png',
            });

            const { total: updatedTotal } = await getTotal(USER_ID).catch(() => ({ total: null }));
            if (updatedTotal != null) setTotal(updatedTotal);

            setShowToast(true);
            setTimeout(() => setShowToast(false), TOAST_DURATION);

            setTimeout(() => {
                navigate("/", {
                    replace: true,
                    state: {
                        toast: {
                            name: prod.name,
                            price: prod.price ?? null,
                            imageUrl: prod.imageUrl ?? null,
                            ean: prod.ean,
                        },
                        total: total ?? null,
                    },
                });
            }, TOAST_DURATION);
        } catch (e) {
            console.error("Scan/Product processing error: ", e);

            let userMessage = "Could not find product or add it to the basket.";

            if (e instanceof Error) {

                if (e.message.includes('{"error": "Failed to retrieve product"')) {
                    userMessage = `Product with ean ${ean} could not be found.`;
                } else {
                    userMessage = `An issue occurred: ${e.message}`;
                }
            }

            setScanError(`Error: ${userMessage}. Please try again or add manually`);
        }
    }

    const { videoRef, status, error, reset } = useBarcodeScanner(handleScan);

    const displayError = scanError || error;

    return (
        <div className="app-bg">
            <div className="phone-frame" style={{ gap: 12}}>
                <h2 className="header" style={{color: '#3b8f5a'}}>Scan a Barcode</h2>

                {displayError && (
                    <div className="card"
                        style={{
                            color: "#f87171",
                            padding: "12px",
                            borderRadius: "8px",
                        }}
                    >
                        <strong style={{ display: 'block', marginBottom: '4px' }}>Scan Error:</strong>
                        {displayError}
                        <button
                            style={{
                                margin: "8px",
                                padding: "6px 10px",
                                background: '#3b8f5a',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '14px',
                            }}
                        >
                            Retry Scan
                        </button>
                    </div>
                )}
                <video
                    ref={videoRef}
                    autoPlay
                    muted
                    playsInline
                    style= {{
                        width: "100%",
                        borderRadius: 16,
                        background: "#111",
                        boxShadow: "0 8px 20px rgba(0, 0, 0, 0.025)",
                        display: status === "scanning" ? "block" : "none",
                    }}
                />

                  {product && status === "done" && (
                      <div className="product-card">
                          {product.imageUrl && (
                              <img src={product.imageUrl} alt={product.name} className="product-image"/>
                          )}
                          <div className="product-info">
                              <div className="product-name">{product.name}</div>
                              {product.brand && <div className="product-brand">{product.brand}</div>}
                              {typeof product.price === "number" && (
                                  <div className="product-price">
                                      {product.price.toFixed(2)}
                                  </div>
                              )}
                              {total != null && (
                                  <div className="product-total">
                                      Basket total: <strong>{total.toFixed(2)}</strong>
                                  </div>
                              )}
                              <button className="product-cta" onClick={reset}>
                                  Scan another item
                              </button>
                          </div>
                      </div>
                  )}

                {product && status === "done" && showToast && (
                    <div className="toast-notification">
                        <div className="toast-content">
                            <div className="toast-icon">✓</div>
                            <div className="toast-body">
                                <div className="toast-title">Added to basket</div>
                                <div className="toast-message">
                                    {product.name}
                                    {product.price && ` • ${product.price.toFixed(2)} NOK`}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}


