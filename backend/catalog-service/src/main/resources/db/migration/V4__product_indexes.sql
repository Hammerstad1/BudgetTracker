CREATE INDEX idx_product_ean ON public.product (ean);
CREATE INDEX idx_product_brand on public.product (brand);
CREATE INDEX idx_product_categoryId on public.product (category_id);
CREATE INDEX idx_product_country_code ON public.product (country_code);