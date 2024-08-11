CREATE DATABASE IF NOT EXISTS `hlhtxc5_dbOfx` 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;

USE `hlhtxc5_dbOfx`;

DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` FUNCTION `optionToOCC`(_UTkr char(12), _dtExpire char(255), 
	_optType char(15), _strikePrice decimal(19,4)) RETURNS char(120) CHARSET utf8
    reads sql data deterministic
BEGIN
	declare occTkr char(120);
    declare tkr char(12);
    declare strike char(120);
    declare iStrike int;
    declare dtExpire date;
    
    set tkr = trim(both from _UTkr);
    set dtExpire = str_to_date(_dtExpire, "%Y%m%d");
    set iStrike = cast(_strikePrice * 1000 as signed);
    set strike = concat("00000000", cast((iStrike) as char));
    
    set occTkr = concat(tkr,
						space(6 - length(tkr)),
                        date_format(dtExpire, "%y%m%d"),
                        left(_optType, 1),
                        right(strike, 8));

	return occTkr;
END ;;

DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `Accounts_returnFId`(in _AccountId int(10))
BEGIN
/* used from application */

Select FId from Accounts, Brokers 

where AcctId = _AccountId
	and Accounts.BrokerId = Brokers.BrokerId
;
END ;;
DELIMITER ;
DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `Brokers_returnFId`(in _BrokerId int(10))
BEGIN
/* used from application */

Select FId from Brokers where BrokerId = _BrokerId;
END ;;
DELIMITER ;
DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `mfSecInfoUpdate`()
BEGIN
/* Set EquityId for mutual funds */
update hlhtxc5_dbOfx.SecInfo
	inner join MFInfo on 
		hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.MFInfo.BrokerId
			and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.MFInfo.SecId

    set EquityId = SecInfo.SecName
;
END ;;
DELIMITER ;
DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `optionsSecInfoUpdate`()
BEGIN
/* Set EquityId for options */
update hlhtxc5_dbOfx.SecInfo
	inner join OptInfo on (hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.OptInfo.BrokerId
    and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.OptInfo.SecId)

	/*set UTkr = option_ticker_trim(SecInfo.SecName), */
    /*set OCCTkr = optionToOCC(SecInfo.SecName, left(OptInfo.DtExpire, 8), 
    OptInfo.OptType, OptInfo.StrikePrice)*/
    set EquityId = optionToOCC(SecInfo.Ticker, left(OptInfo.DtExpire, 8), 
    OptInfo.OptType, OptInfo.StrikePrice)
    
where left(SecInfo.Ticker, 1) <> "#";
END ;;
DELIMITER ;
DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `stockSecInfoUpdate`()
BEGIN
/* Set EquityId for stocks */
update hlhtxc5_dbOfx.SecInfo
	inner join StockInfo on SecInfo.BrokerId = StockInfo.BrokerId
    and SecInfo.SecId = StockInfo.SecId

    set EquityId = SecInfo.Ticker
;
END ;;
DELIMITER ;
DELIMITER ;;
CREATE DEFINER=`hlhtxc5`@`localhost` PROCEDURE `truncate_dbOfx`()
BEGIN
truncate table PosDebt;
truncate table PosMF;
truncate table PosOpt;
truncate table PosOther;
truncate table PosStock;
truncate table Bal;
truncate table InvBal;
truncate table InvPos;
END ;;
DELIMITER ;
