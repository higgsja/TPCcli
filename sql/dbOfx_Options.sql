(SELECT 
Accts.JoomlaId, Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

InvBuy.Units, InvBuy.UnitPrice as TradePrice, InvBuy.Markup as MarkUpDn, InvBuy.Commission, InvBuy.Taxes, InvBuy.Fees, InvBuy.Total,


Br.BrokerId, 
left(InvTran.DtTrade, 10) as DtTrade, Bo.FiTId, Bo.OptBuyType as OptTransactionType, Bo.ShPerCtrct, 
InvTran.Skip

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.BuyOpt as Bo, hlhtxc5_dbOfx.InvTran as InvTran,
    hlhtxc5_dbOfx.InvBuy, hlhtxc5_dbOfx.SecInfo as SecInfo


WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Bo.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Bo.FiTId = InvTran.FiTId
    and Accts.AcctId = InvBuy.AcctId
    and Bo.FiTId = InvBuy.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and InvBuy.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' and Accts.AcctId = '1')

union

(SELECT 
Accts.JoomlaId, Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

Co.Units, 0.0 as TradePrice, 0.0 as MarkUpDn, 0.0 as Commission, 0.0 as Taxes, 0.0 as Fees, 0.0 as Total,

Br.BrokerId, 
left(InvTran.DtTrade, 10) as DtTrade, Co.FiTId, Co.OptAction as OptTransactionType, Co.ShPerCtrct, InvTran.Skip

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.ClosureOpt as Co, hlhtxc5_dbOfx.InvTran as InvTran,
  hlhtxc5_dbOfx.SecInfo as SecInfo


WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Co.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Co.FiTId = InvTran.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and Co.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' and Accts.AcctId = '1')

union

(SELECT 
Accts.JoomlaId, Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice as LastPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

InvSell.Units, InvSell.UnitPrice as TradePrice, InvSell.Markdown as MarkUpDn, InvSell.Commission, InvSell.Taxes, InvSell.Fees, InvSell.Total,

Br.BrokerId, 
left(InvTran.DtTrade, 10) as DtTrade, Bo.FiTId, Bo.OptSellType as OptTransactionType, Bo.ShPerCtrct, InvTran.Skip

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.SellOpt as Bo, hlhtxc5_dbOfx.InvTran as InvTran,
    hlhtxc5_dbOfx.InvSell, hlhtxc5_dbOfx.SecInfo as SecInfo

WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Bo.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Bo.FiTId = InvTran.FiTId
    and Accts.AcctId = InvSell.AcctId
    and Bo.FiTId = InvSell.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and InvSell.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' and Accts.AcctId = '1')
    
order by EquityId, FiTId
