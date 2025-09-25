import "./style.css";
import { useNavigate } from "react-router-dom";


export default function App() {
    const navigate = useNavigate();

  return (
      <div className="app-bg">
        <div className="phone-frame">
          <header className="header">
              <h1>Grocery store list</h1>
          </header>

          <section className="list-panel">
              <div className="list-item"></div>
              <div className="list-item"></div>
              <div className="list-item"></div>
          </section>

          <div className="price-row">
              <div className="price-label">Total price: </div>
              <div className="price-box"></div>
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
      </div>

  )
}

