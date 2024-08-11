(select Accts.JoomlaId, Accts.AcctId, SecInfo.EquityId, 
    /*SecInfo.Ticker,*/ SecInfo.SecName, SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 8) as DtAsOf, InvBuy.Units, InvBuy.UnitPrice as TradePrice, 
    /*InvBuy.Markup as MarkUpDn, InvBuy.Commission, InvBuy.Taxes, InvBuy.Fees,*/ InvBuy.Total, /*Br.BrokerId,*/ left(InvTran.DtTrade, 8) as DtTrade, Bo.FiTId, 
    Bo.OptBuyType as OptTransactionType, Bo.ShPerCtrct, InvTran.Skip /*InvTran.Validated, InvTran.Complete*/ from hlhtxc5_dbOfx.Brokers as Br, 
    hlhtxc5_dbOfx.Accounts as Accts, hlhtxc5_dbOfx.BuyOpt as Bo, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.InvBuy, hlhtxc5_dbOfx.SecInfo as SecInfo 
    where Br.BrokerId = Accts.BrokerId and Accts.AcctId = Bo.AcctId 
    and Accts.AcctId = InvTran.AcctId and Bo.FiTId = InvTran.FiTId and Accts.AcctId = InvBuy.AcctId 
    and Bo.FiTId = InvBuy.FiTId and Br.BrokerId = SecInfo.BrokerId and InvBuy.SecId = SecInfo.SecId and Accts.AcctId = 2 /*and EquityId like '%s%%'*/ and Accts.JoomlaId = 816
    and (substring(Bo.FiTId, 1, 2) = "22" or substring(Bo.FiTId, 1, 2) = "23")
    and InvTran.Skip = 0
) 

union (select Accts.JoomlaId, Accts.AcctId, SecInfo.EquityId, /*SecInfo.Ticker,*/ SecInfo.SecName, SecInfo.UnitPrice as LastPrice, 
    left(SecInfo.DtAsOf, 8) as DtAsOf, Co.Units, 0.0 as TradePrice, /*0.0 as MarkUpDn, 0.0 as Commission, 0.0 as Taxes, 0.0 as Fees,*/ 0.0 as Total, /*Br.BrokerId, */
    left(InvTran.DtTrade, 8) as DtTrade, Co.FiTId, Co.OptAction as OptTransactionType, Co.ShPerCtrct, InvTran.Skip /*InvTran.Validated, InvTran.Complete*/ 
    from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts, 
    hlhtxc5_dbOfx.ClosureOpt as Co, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.SecInfo as SecInfo 
    where Br.BrokerId = Accts.BrokerId and Accts.AcctId = Co.AcctId and Accts.AcctId = InvTran.AcctId 
    and Co.FiTId = InvTran.FiTId and Br.BrokerId = SecInfo.BrokerId and Co.SecId = SecInfo.SecId and Accts.AcctId = 2 /*and EquityId like '%s%%'*/ and Accts.JoomlaId = 816
    and (substring(Co.FiTId, 1, 2) = "22" or substring(Co.FiTId, 1, 2) = "23")
    and InvTran.Skip = 0
)

    union (select Accts.JoomlaId, Accts.AcctId, SecInfo.EquityId, /*SecInfo.Ticker,*/ SecInfo.SecName, SecInfo.UnitPrice as LastPrice, 
    left(SecInfo.DtAsOf, 8) as DtAsOf, InvSell.Units, InvSell.UnitPrice as TradePrice, /*InvSell.Markdown as MarkUpDn, InvSell.Commission, InvSell.Taxes, InvSell.Fees,*/ InvSell.Total, 
    /*Br.BrokerId,*/ left(InvTran.DtTrade, 8) as DtTrade, Bo.FiTId, Bo.OptSellType as OptTransactionType, Bo.ShPerCtrct, InvTran.Skip /*InvTran.Validated, InvTran.Complete*/ 
    from hlhtxc5_dbOfx.Brokers as Br, 
    hlhtxc5_dbOfx.Accounts as Accts, hlhtxc5_dbOfx.SellOpt as Bo, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.InvSell, hlhtxc5_dbOfx.SecInfo as SecInfo 
    where Br.BrokerId = Accts.BrokerId and Accts.AcctId = Bo.AcctId and Accts.AcctId = InvTran.AcctId and Bo.FiTId = InvTran.FiTId and Accts.AcctId = InvSell.AcctId 
    and Bo.FiTId = InvSell.FiTId and Br.BrokerId = SecInfo.BrokerId and InvSell.SecId = SecInfo.SecId and Accts.AcctId = 2 /*and EquityId like '%s%%'*/ and Accts.JoomlaId = 816
        and (substring(Bo.FiTId, 1, 2) = "22" or substring(Bo.FiTId, 1, 2) = "23")
    and InvTran.Skip = 0
) 

union /* ClientClosingOption */ 
(select cco.JoomlaId, Accts.AcctId, cco.EquityId, /*cco.Ticker,*/ '' as SecName, '' as LastPrice, '' as DtAsOf, cco.Units, cco.PriceOpen as TradePrice, /*'' as MarkUpDn, '' as Commission, 
    '' as Taxes, '' as Fees,*/ '' as Total, /*'' as BrokerId,*/ DateOpen as DtTrade, cco.FiTId, TransactionType as TransactionType, '100', '0' as Skip /*'1' as Validated, '0' as Complete*/ 
    from hlhtxc5_dmOfx.ClientClosingOptions as cco, hlhtxc5_dmOfx.Accounts as Accts 
    where cco.DMAcctId = Accts.DMAcctId and cco.JoomlaId = Accts.JoomlaId and Accts.AcctId = 2 /*and EquityId like '%s%%'*/ and Accts.JoomlaId = 816
        and (substring(cco.FiTId, 1, 2) = "22" or substring(cco.FiTId, 1, 2) = "23")
) 

union /* ClientOpeningOption */ 
(select coo.JoomlaId, Accts.AcctId, coo.EquityId, /*coo.Ticker,*/ '' as SecName, '' as LastPrice, '' as DtAsOf, coo.Units, coo.PriceOpen as TradePrice, /*'' as MarkUpDn, '' as Commission, 
    '' as Taxes, '' as Fees,*/ '' as Total, /*'' as BrokerId,*/ DateOpen as DtTrade, coo.FiTId, TransactionType as TransactionType, '100', '0' as Skip /*'1' as Validated, '0' as Complete*/ 
    from hlhtxc5_dmOfx.ClientOpeningOptions as coo, hlhtxc5_dmOfx.Accounts as Accts 
    where coo.DMAcctId = Accts.DMAcctId and coo.JoomlaId = Accts.JoomlaId and Accts.AcctId = 2 /*and EquityId like '%s%%'*/ and Accts.JoomlaId = 816
        and (substring(coo.FiTId, 1, 2) = "22" or substring(coo.FiTId, 1, 2) = "23")
) 

order by EquityId, FiTId