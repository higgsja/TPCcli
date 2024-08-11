SELECT 
Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.SecId,

InvSell.Units, 


InvTran.DtTrade, Bo.FiTId, Bo.OptSellType,   
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
    and Accts.JoomlaId = '816' 
    and Accts.AcctId = '1'
    and DtTrade > "20240601170000.000"
	and DtTrade < "20240630170000.000"
    and SecInfo.Ticker = "nvda"
    
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId