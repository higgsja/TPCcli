SELECT Accts.AcctId, SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, 
InvBuy.Units, InvBuy.UnitPrice, InvBuy.Total, Br.BrokerId, 
InvTran.DtTrade, Bs.FiTId, Bs.BuyType as StockTransType, 
InvTran.Skip 

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.BuyStock as Bs, hlhtxc5_dbOfx.InvTran as InvTran,
    hlhtxc5_dbOfx.InvBuy InvBuy, hlhtxc5_dbOfx.SecInfo as SecInfo 
    
WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Bs.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Bs.FiTId = InvTran.FiTId
    and Accts.AcctId = InvBuy.AcctId
    and Bs.FiTId = InvBuy.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and InvBuy.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' 
    and Accts.AcctId = '1'
    #and DtTrade > "20230101170000.000"
	#and DtTrade < "20240901170000.000"
    and SecInfo.EquityId = "msft"    

order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId;
