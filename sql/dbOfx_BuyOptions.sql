SELECT 
Accts.JoomlaId, Accts.AcctId, 


SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

InvBuy.Units, InvBuy.UnitPrice, InvBuy.Markup as MarkUpDn, InvBuy.Commission, InvBuy.Taxes, InvBuy.Fees, InvBuy.Total,


Br.BrokerId, 
InvTran.DtTrade, Bo.FiTId, Bo.OptBuyType as OptTransType, Bo.ShPerCtrct, InvTran.DtTrade, 
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
    and Accts.JoomlaId = '816' and Accts.AcctId = '1'
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId