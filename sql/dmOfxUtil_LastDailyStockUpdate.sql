INSERT IGNORE INTO hlhtxc5_dmOfx.Util_LastDailyStock (EquityId, `Date`, Open, High, Low, Close, Volume)
SELECT eh.Ticker, eh.`Date`, eh.`Open`, eh.High, eh.Low, eh.`Close`, eh.Volume
FROM hlhtxc5_dmOfx.EquityHistory eh
JOIN (
    SELECT Ticker, MAX(`Date`) AS MaxDate
    FROM hlhtxc5_dmOfx.EquityHistory
    GROUP BY Ticker
) latest ON eh.Ticker = latest.Ticker AND eh.`Date` = latest.MaxDate
ORDER BY eh.Ticker, eh.`Date`;