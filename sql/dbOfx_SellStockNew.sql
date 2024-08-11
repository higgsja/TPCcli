SELECT 
Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.SecId, 
InvSell.Units, InvSell.UnitPrice, InvSell.Total,
Br.BrokerId, 
InvTran.DtTrade, Ss.FiTId, Ss.SellType as StockTransType, 
InvTran.Skip

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.SellStock as Ss, hlhtxc5_dbOfx.InvTran as InvTran,
    hlhtxc5_dbOfx.InvSell InvSell, hlhtxc5_dbOfx.SecInfo as SecInfo


WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Ss.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Ss.FiTId = InvTran.FiTId
    and Accts.AcctId = InvSell.AcctId
    and Ss.FiTId = InvSell.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and InvSell.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' 
    #and Accts.AcctId = '1'
    and DtTrade > "20230101170000.000"
	and DtTrade < "20240901170000.000"

    and SecInfo.EquityId = "avgo"
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId