#use to cause complete regen of dataMart from dbOfx
#improve this by removing by JoomlaId instead
#commented rows are things that should be optional
#truncate hlhtxc5_dmOfx.AccountTotals;
#truncate hlhtxc5_dmOfx.AppTracking;
#truncate hlhtxc5_dmOfx.Balances;
#truncate hlhtxc5_dmOfx.ClientEquityAttributes;
#truncate hlhtxc5_dmOfx.ClientSectorList;
truncate hlhtxc5_dmOfx.ClosedOptionFIFO;
truncate hlhtxc5_dmOfx.ClosedOptionTrans;
truncate hlhtxc5_dmOfx.ClosedStockFIFO;
truncate hlhtxc5_dmOfx.ClosedStockTrans;
truncate hlhtxc5_dmOfx.ClosingDebt;
truncate hlhtxc5_dmOfx.ClosingMF;
truncate hlhtxc5_dmOfx.ClosingOptions;
truncate hlhtxc5_dmOfx.ClosingOther;
truncate hlhtxc5_dmOfx.ClosingStock;
#truncate hlhtxc5_dmOfx.Drawdown;
truncate hlhtxc5_dmOfx.FIFOClosedTransactions;
truncate hlhtxc5_dmOfx.FIFOOpenTransactions;
truncate hlhtxc5_dmOfx.OpeningDebt;
truncate hlhtxc5_dmOfx.OpeningMF;
truncate hlhtxc5_dmOfx.OpeningOptions;
truncate hlhtxc5_dmOfx.OpeningOther;
truncate hlhtxc5_dmOfx.OpeningStock;
truncate hlhtxc5_dmOfx.OpenOptionFIFO;
truncate hlhtxc5_dmOfx.OpenStockFIFO;
truncate hlhtxc5_dmOfx.PositionsClosed;
truncate hlhtxc5_dmOfx.PositionsClosedTransactions;
truncate hlhtxc5_dmOfx.PositionsOpen;
truncate hlhtxc5_dmOfx.PositionsOpenTransactions;
truncate hlhtxc5_dmOfx.TransactionLog;
update hlhtxc5_dbOfx.InvTran set Complete = 0;
update hlhtxc5_dmOfx.clientOpeningStock set Units = OrigUnits;
update hlhtxc5_dmOfx.clientClosingStock set Units = OrigUnits;
