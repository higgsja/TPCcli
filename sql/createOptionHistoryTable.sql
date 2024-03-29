use hlhtxc5_dmOfx;
create table OptionHistory (
adjustedFlag boolean,
ask decimal(19,4),
askSize int(10),
askTime varchar(30),
bid decimal(19,4),
bidExchange varchar(30),
bidSize int(10),
bidTime varchar(30),
changeClose decimal(19,4),
changeClosePercentage decimal(19,4),
companyName varchar(40),
daysToExpiration int(10),
dirLast varchar(30),
dividend decimal(19,4),
eps decimal(19,4),
estEarnings decimal(19,4),
exDividendDate int(12),
high decimal(19,4),
high52 decimal(19,4),
lastTrade decimal(19,4),
low decimal(19,4),
low52 decimal(19,4),
`open` decimal(19,4),
openInterest int(10),
optionStyle varchar(10),
optionUnderlier varchar(10),
optionUnderlierExchange varchar(30),
previousClose decimal(19,4),
previousDayVolume int(20),
primaryExchange varchar(30),
symbolDescription varchar(50),
totalVolume int(20),
upc int(20),
//optionDeliverableListArray ,
cashDeliverable decimal(19,4),
marketCap decimal(19,4),
sharesOutstanding int(20),
nextEarningDate varchar(12),
beta decimal(19,4),
yield decimal(19,4),
declaredDividend decimal(19,4),
dividendPayableDate int(12),
pe decimal(19,4),
week52LowDate int(12),
week52HiDate int(12),
intrinsicValue decimal(19,4),
timePremium decimal(19,4),
optionMultiplier decimal(19,4),
contractSize decimal(19,4),
expirationDate int(12),
//ehQuoteArray ,
optionPreviousBidPrice decimal(19,4),
optionPreviousAskPrice decimal(19,4),
osiKey varchar(20),
timeOfLastTrade int(12),
averageVolume int(20)


)