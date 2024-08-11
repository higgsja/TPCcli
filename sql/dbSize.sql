use dmOfx;
SELECT table_schema,
        ROUND(SUM(data_length + index_length) / 1024 / 1024, 1)
FROM information_schema.tables 
where table_schema='dmOfx'
GROUP BY table_schema; 