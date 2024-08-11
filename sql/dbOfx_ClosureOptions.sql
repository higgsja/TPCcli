SELECT 
Accts.JoomlaId, Accts.AcctId, 

SecInfo.EquityId, SecInfo.Ticker, SecInfo.SecName, SecInfo.UnitPrice, left(SecInfo.DtAsOf, 10) as DtAsOf,

Co.Units, 0.0 as UnitPrice, 0.0 as MarkUpDn, 0.0 as Commission, 0.0 as Taxes, 0.0 as Fees, 0.0 as Total,

Br.BrokerId, 
InvTran.DtTrade, Co.FiTId, Co.OptAction as OptTranType, Co.ShPerCtrct, InvTran.DtTrade, InvTran.Skip

from hlhtxc5_dbOfx.Brokers as Br, hlhtxc5_dbOfx.Accounts as Accts,
	hlhtxc5_dbOfx.ClosureOpt as Co, hlhtxc5_dbOfx.InvTran as InvTran,
  hlhtxc5_dbOfx.SecInfo as SecInfo


WHERE Br.BrokerId = Accts.BrokerId 
	and Accts.AcctId = Co.AcctId
	and Accts.AcctId = InvTran.AcctId
	and Co.FiTId = InvTran.FiTId
    and Br.BrokerId = SecInfo.BrokerId
    and Co.SecId = SecInfo.SecId
    and Accts.JoomlaId = '816' and Accts.AcctId = '1'
    
order by SecInfo.EquityId, InvTran.DtTrade, InvTran.FiTId 