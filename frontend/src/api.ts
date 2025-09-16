export const API_BASE =
    import.meta.env.VITE_API_BASE && import.meta.env.VITE_API_BASE.trim() !== ""
        ? import.meta.env.VITE_API_BASE
        : "/api";

