SELECT 
Accts.JoomlaId, Accts.AcctId, 


SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

InvSell.Units, InvSell.UnitPrice, InvSell.Markdown as MarkUpDn, InvSell.Commission, InvSell.Taxes, InvSell.Fees, InvSell.Total,


Br.BrokerId, 
InvTran.DtTrade, Bo.FiTId, Bo.OptSellType, Bo.ShPerCtrct, InvTran.DtTrade, 
InvTran.Skip

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
    and Accts.JoomlaId = '816' and Accts.AcctId = '1'
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId
