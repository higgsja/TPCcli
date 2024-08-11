SELECT 
Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.SecId, 

InvBuy.Units, 

InvTran.DtTrade, Bo.FiTId, Bo.OptBuyType as OptTransType, 
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
    and Accts.JoomlaId = '816' 
    #and Accts.AcctId = '1'
    and DtTrade > "20240601170000.000"
	and DtTrade < "20240630170000.000"
    and SecInfo.Ticker = "nvda"
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId