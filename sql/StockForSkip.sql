(select Accts.JoomlaId, Accts.AcctId, SecInfo.EquityId, /*SecInfo.Ticker,*/ 
    /*SecInfo.SecName,*/ SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 8) as DtAsOf, InvBuy.Units, InvBuy.UnitPrice as TradePrice, /*InvBuy.Markup as MarkUpDn, InvBuy.Commission, 
    InvBuy.Taxes, InvBuy.Fees,*/ InvBuy.Total, /*Br.BrokerId,*/ left(InvTran.DtTrade, 8) as DtTrade, bs.FiTId, bs.BuyType as TransactionType, InvTran.Skip, InvTran.Validated, InvTran.Complete 

    from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts, hlhtxc5_dbOfx.BuyStock as bs, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.InvBuy, 
    hlhtxc5_dbOfx.SecInfo as SecInfo 

    where Br.BrokerId = Accts.BrokerId 
    and Accts.AcctId = bs.AcctId and Accts.AcctId = InvTran.AcctId and bs.FiTId = InvTran.FiTId and Accts.AcctId = InvBuy.AcctId 
    and bs.FiTId = InvBuy.FiTId and Br.BrokerId = SecInfo.BrokerId and InvBuy.SecId = SecInfo.SecId and Accts.AcctId = 1 /*and EquityId = '%s'*/ and Accts.JoomlaId = 816
    and substring(bs.FiTId, 1, 2) = "22"
) 

union 
(select Accts.JoomlaId, Accts.AcctId, SecInfo.EquityId, /*SecInfo.Ticker, SecInfo.SecName,*/ 
    SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 8) as DtAsOf, InvSell.Units, InvSell.UnitPrice as TradePrice, /*InvSell.Markdown as MarkUpDn, InvSell.Commission, InvSell.Taxes, 
    InvSell.Fees,*/ InvSell.Total, /*Br.BrokerId,*/ left(InvTran.DtTrade, 8) as DtTrade, ss.FiTId, ss.SellType as TransactionType, InvTran.Skip, InvTran.Validated, InvTran.Complete 

    from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts, hlhtxc5_dbOfx.SellStock as ss, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.InvSell, 
    hlhtxc5_dbOfx.SecInfo as SecInfo 
    where Br.BrokerId = Accts.BrokerId and Accts.AcctId = ss.AcctId and Accts.AcctId = InvTran.AcctId and ss.FiTId = InvTran.FiTId and Accts.AcctId = InvSell.AcctId 
    and ss.FiTId = InvSell.FiTId and Br.BrokerId = SecInfo.BrokerId and InvSell.SecId = SecInfo.SecId and Accts.AcctId = 1 /*and EquityId = '%s'*/ and Accts.JoomlaId = 816
    and substring(ss.FiTId, 1, 2) = "22"
) 

union /* ClientClosingStock */ 
(select ccs.JoomlaId, Accts.AcctId, ccs.EquityId, /*ccs.Ticker, '' as SecName,*/ '' as LastPrice, '' as DtAsOf, ccs.Units, ccs.PriceOpen as TradePrice, /*'' as MarkUpDn, '' as Commission,
    '' as Taxes, '' as Fees,*/ '' as Total, /*'' as BrokerId,*/ DateOpen as DtTrade, ccs.FiTId, TransactionType as TransactionType, '0' as Skip, '1' as Validated, '0' as Complete 
    from hlhtxc5_dmOfx.ClientClosingStock as ccs, hlhtxc5_dmOfx.Accounts as Accts 
    where ccs.DMAcctId = Accts.DMAcctId and ccs.JoomlaId = Accts.JoomlaId and Accts.AcctId = 1 /*and EquityId = '%s'*/ and Accts.JoomlaId = 816
) 

union /* ClientOpeningStock */ 
(select cos.JoomlaId, Accts.AcctId, cos.EquityId, /*cos.Ticker, '' as SecName,*/ '' as LastPrice, '' as DtAsOf, cos.Units, cos.PriceOpen as TradePrice, /*'' as MarkUpDn, '' as Commission, 
    '' as Taxes, '' as Fees,*/ '' as Total, /*'' as BrokerId,*/ DateOpen as DtTrade, cos.FiTId, TransactionType as TransactionType, '0' as Skip, '1' as Validated, '0' as Complete 
    from hlhtxc5_dmOfx.ClientOpeningStock as cos, hlhtxc5_dmOfx.Accounts as Accts 
    where cos.DMAcctId = Accts.DMAcctId and cos.JoomlaId = Accts.JoomlaId and Accts.AcctId = 1 /*and EquityId = '%s'*/ and Accts.JoomlaId = 816
) 
order by EquityId, FiTId