import { BrowserRouter, Routes, Route } from "react-router-dom";
import ProductListPage from "./pages/ProductListPage";
import ProductDetailsPage from "./pages/ProductDetailsPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route
                    path="/"
                    element={<ProductListPage />}
                />

                <Route
                    path="/products/:id"
                    element={<ProductDetailsPage />}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;