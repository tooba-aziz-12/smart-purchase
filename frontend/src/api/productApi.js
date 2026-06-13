const BASE_URL = "http://localhost:8095";

export async function fetchProducts(filters) {

    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
        if (value !== "" && value !== null) {
            params.append(key, value);
        }
    });

    const response = await fetch(
        `${BASE_URL}/products?${params.toString()}`
    );

    return response.json();
}

export async function fetchProductDetails(id) {

    const response = await fetch(
        `${BASE_URL}/products/${id}`
    );

    return response.json();
}

export async function fetchSimilarProducts(id) {

    const response = await fetch(
        `${BASE_URL}/products/${id}/similar`
    );

    return response.json();
}