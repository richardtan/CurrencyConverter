package com.tckr.currencyconverter.data;

public class CurrencyData {

	private int currencyIndex;
	private int currencyIndexOld;
	private String currency = "";
	private String currencyDisplay = "";
	private int baseIndex;
	
	public CurrencyData(int currencyIndex, int currencyIndexOld, String currency, String currencyDisplay, int baseIndex) {
		this.currencyIndex = currencyIndex;
		this.currencyIndexOld = currencyIndexOld;
		this.currency = currency;
		this.currencyDisplay = currencyDisplay;
		this.baseIndex = baseIndex;
	}

	public String toString() {
		return this.currencyDisplay;
	}

	public int getCurrencyIndex() {
		return currencyIndex;
	}
	
	/**
	 * This gets the old Currency Index for the Cursor Array. We need this to update tables for version code < 2.
	 * @return
	 */
	public int getCurrencyIndexOld() {
		return currencyIndexOld;
	}
	
	public String getCurrency() {
		return currency;
	}

	public String getCurrencyDisplay() {
		return currencyDisplay;
	}
	
	public int getBaseIndex() {
		return baseIndex;
	}
	
	public static CurrencyData[] populateData() {
		
		CurrencyData[] currencyData = new CurrencyData[] {
				
				/*
				 * The Cursor Adaptor will display the data as it receives it, therefore we will need to organise the data
				 * So the below lines commented was the order from version 1.0 This has been commented out with the new
				 * list underneath.
				 *
				new CurrencyData(0, "AED", "United Arab Emirates Dirham (AED)", 1),
				new CurrencyData(1, "ANG", "Netherlands Antillean Guilder (ANG)", 1),
				new CurrencyData(2, "ARS", "Argentine Peso (ARS)", 1),
				new CurrencyData(3, "AUD", "Australian Dollar (AUD)", 1),
				new CurrencyData(4, "BGN", "Bulgarian Lev (BGN)", 1),
				new CurrencyData(5, "BHD", "Bahraini Dinar (BHD)", 1),
				new CurrencyData(6, "BND", "Brunei Dollar (BND)", 1),
				new CurrencyData(7, "BOB", "Bolivian Boliviano (BOB)", 1),
				new CurrencyData(8, "BRL", "Brazilian Real (BRL)", 1),
				new CurrencyData(9, "BWP", "Botswanan Pula (BWP)", 1),
				new CurrencyData(10, "CAD", "Canadian Dollar (CAD)", 1),
				new CurrencyData(11, "CHF", "Swiss Franc (CHF)", 1),
				new CurrencyData(12, "CLP", "Chilean Peso (CLP)", 1),
				new CurrencyData(13, "CNY", "Chinese Yuan (CNY)", 1),
				new CurrencyData(14, "COP", "Colombian Peso (COP)", 10),
				new CurrencyData(15, "CRC", "Costa Rican Colon (CRC)", 1),
				new CurrencyData(16, "CZK", "Czech Republic Koruna (CZK)", 1),
				new CurrencyData(17, "DKK", "Danish Krone (DKK)", 1),
				new CurrencyData(18, "DOP", "Dominican Peso (DOP)", 1),
				new CurrencyData(19, "DZD", "Algerian Dinar (DZD)", 1),
				new CurrencyData(20, "EEK", "Estonian Kroon (EEK)", 1),
				new CurrencyData(21, "EGP", "Egyptian Pound (EGP)", 1),
				new CurrencyData(22, "EUR", "Euro (EUR)", 1),
				new CurrencyData(23, "FJD", "Fijian Dollar (FJD)", 1),
				new CurrencyData(24, "GBP", "British Pound Sterling (GBP)", 1),
				new CurrencyData(25, "HKD", "Hong Kong Dollar (HKD)", 1),
				new CurrencyData(26, "HNL", "Honduran Lempira (HNL)", 1),
				new CurrencyData(27, "HRK", "Croatian Kuna (HRK)", 1),
				new CurrencyData(28, "HUF", "Hungarian Forint (HUF)", 1),
				new CurrencyData(29, "IDR", "Indonesian Rupiah (IDR)", 10),
				new CurrencyData(30, "ILS", "Israeli New Sheqel (ILS)", 1),
				new CurrencyData(31, "INR", "Indian Rupee (INR)", 1),
				new CurrencyData(32, "JMD", "Jamaican Dollar (JMD)", 1),
				new CurrencyData(33, "JOD", "Jordanian Dinar (JOD)", 1),
				new CurrencyData(34, "JPY", "Japanese Yen (JPY)", 1),
				new CurrencyData(35, "KES", "Kenyan Shilling (KES)", 1),
				new CurrencyData(36, "KRW", "South Korean Won (KRW)", 10),
				new CurrencyData(37, "KWD", "Kuwaiti Dinar (KWD)", 1),
				new CurrencyData(38, "KYD", "Cayman Islands Dollar (KYD)", 1),
				new CurrencyData(39, "KZT", "Kazakhstani Tenge (KZT)", 1),
				new CurrencyData(40, "LBP", "Lebanese Pound (LBP)", 10),
				new CurrencyData(41, "LKR", "Sri Lankan Rupee (LKR)", 1),
				new CurrencyData(42, "LTL", "Lithuanian Litas (LTL)", 1),
				new CurrencyData(43, "LVL", "Latvian Lats (LVL)", 1),
				new CurrencyData(44, "MAD", "Moroccan Dirham (MAD)", 1),
				new CurrencyData(45, "MDL", "Moldovan Leu (MDL)", 1),
				new CurrencyData(46, "MKD", "Macedonian Denar (MKD)", 1),
				new CurrencyData(47, "MUR", "Mauritian Rupee (MUR)", 1),
				new CurrencyData(48, "MXN", "Mexican Peso (MXN)", 1),
				new CurrencyData(49, "MYR", "Malaysian Ringgit (MYR)", 1),
				new CurrencyData(50, "NAD", "Namibian Dollar (NAD)", 1),
				new CurrencyData(51, "NGN", "Nigerian Naira (NGN)", 1),
				new CurrencyData(52, "NIO", "Nicaraguan Cordoba (NIO)", 1),
				new CurrencyData(53, "NOK", "Norwegian Krone (NOK)", 1),
				new CurrencyData(54, "NPR", "Nepalese Rupee (NPR)", 1),
				new CurrencyData(55, "NZD", "New Zealand Dollar (NZD)", 1),
				new CurrencyData(56, "OMR", "Omani Rial (OMR)", 1),
				new CurrencyData(57, "PEN", "Peruvian Nuevo Sol (PEN)", 1),
				new CurrencyData(58, "PGK", "Papua New Guinean Kina (PGK)", 1),
				new CurrencyData(59, "PHP", "Philippine Peso (PHP)", 1),
				new CurrencyData(60, "PKR", "Pakistani Rupee (PKR)", 1),
				new CurrencyData(61, "PLN", "Polish Zloty (PLN)", 1),
				new CurrencyData(62, "PYG", "Paraguayan Guarani (PYG)", 10),
				new CurrencyData(63, "QAR", "Qatari Rial (QAR)", 1),
				new CurrencyData(64, "RON", "Romanian Leu (RON)", 1),
				new CurrencyData(65, "RSD", "Serbian Dinar (RSD)", 1),
				new CurrencyData(66, "RUB", "Russian Ruble (RUB)", 1),
				new CurrencyData(67, "SAR", "Saudi Riyal (SAR)", 1),
				new CurrencyData(68, "SCR", "Seychellois Rupee (SCR)", 1),
				new CurrencyData(69, "SEK", "Swedish Krona (SEK)", 1),
				new CurrencyData(70, "SGD", "Singapore Dollar (SGD)", 1),
				new CurrencyData(71, "SKK", "Slovak Koruna (SKK)", 1),
				new CurrencyData(72, "SLL", "Sierra Leonean Leone (SLL)", 10),
				new CurrencyData(73, "SVC", "Salvadoran Colon (SVC)", 1),
				new CurrencyData(74, "THB", "Thai Baht (THB)", 1),
				new CurrencyData(75, "TND", "Tunisian Dinar (TND)", 1),
				new CurrencyData(76, "TRY", "Turkish Lira (TRY)", 1),
				new CurrencyData(77, "TTD", "Trinidad and Tobago Dollar (TTD)", 1),
				new CurrencyData(78, "TWD", "New Taiwan Dollar (TWD)", 1),
				new CurrencyData(79, "TZS", "Tanzanian Shilling (TZS)", 10),
				new CurrencyData(80, "UAH", "Ukrainian Hryvnia (UAH)", 1),
				new CurrencyData(81, "UGX", "Ugandan Shilling (UGX)", 10),
				new CurrencyData(82, "USD", "US Dollar (USD)", 1),
				new CurrencyData(83, "UYU", "Uruguayan Peso (UYU)", 1),
				new CurrencyData(84, "UZS", "Uzbekistan Som (UZS)", 10),
				new CurrencyData(85, "VEF", "Venezuelan Bolivar (VEF)", 1),
				new CurrencyData(86, "VND", "Vietnamese Dong (VND)", 100),
				new CurrencyData(87, "YER", "Yemeni Rial (YER)", 1),
				new CurrencyData(88, "ZAR", "South African Rand (ZAR)", 1),
				new CurrencyData(89, "ZMK", "Zambian Kwacha (ZMK)", 10)
				*/
				
				new CurrencyData(0, 19, "DZD", "Algerian Dinar (DZD)", 1),
				new CurrencyData(1, 2, "ARS", "Argentine Peso (ARS)", 1),
				new CurrencyData(2, 3, "AUD", "Australian Dollar (AUD)", 1),
				new CurrencyData(3, 5, "BHD", "Bahraini Dinar (BHD)", 1),
				new CurrencyData(4, 7, "BOB", "Bolivian Boliviano (BOB)", 1),
				new CurrencyData(5, 9, "BWP", "Botswanan Pula (BWP)", 1),
				new CurrencyData(6, 8, "BRL", "Brazilian Real (BRL)", 1),
				new CurrencyData(7, 24, "GBP", "British Pound Sterling (GBP)", 1),
				new CurrencyData(8, 6, "BND", "Brunei Dollar (BND)", 1),
				new CurrencyData(9, 4, "BGN", "Bulgarian Lev (BGN)", 1),
				new CurrencyData(10, 10, "CAD", "Canadian Dollar (CAD)", 1),
				new CurrencyData(11, 38, "KYD", "Cayman Islands Dollar (KYD)", 1),
				new CurrencyData(12, 12, "CLP", "Chilean Peso (CLP)", 1),
				new CurrencyData(13, 13, "CNY", "Chinese Yuan (CNY)", 1),
				new CurrencyData(14, 14, "COP", "Colombian Peso (COP)", 10),
				new CurrencyData(15, 15, "CRC", "Costa Rican Colon (CRC)", 1),
				new CurrencyData(16, 27, "HRK", "Croatian Kuna (HRK)", 1),
				new CurrencyData(17, 16, "CZK", "Czech Republic Koruna (CZK)", 1),
				new CurrencyData(18, 17, "DKK", "Danish Krone (DKK)", 1),
				new CurrencyData(19, 18, "DOP", "Dominican Peso (DOP)", 1),
				new CurrencyData(20, 21, "EGP", "Egyptian Pound (EGP)", 1),
				new CurrencyData(21, 20, "EEK", "Estonian Kroon (EEK)", 1),
				new CurrencyData(22, 22, "EUR", "Euro (EUR)", 1),
				new CurrencyData(23, 23, "FJD", "Fijian Dollar (FJD)", 1),
				new CurrencyData(24, 26, "HNL", "Honduran Lempira (HNL)", 1),
				new CurrencyData(25, 25, "HKD", "Hong Kong Dollar (HKD)", 1),
				new CurrencyData(26, 28, "HUF", "Hungarian Forint (HUF)", 1),
				new CurrencyData(27, 31, "INR", "Indian Rupee (INR)", 1),
				new CurrencyData(28, 29, "IDR", "Indonesian Rupiah (IDR)", 10),
				new CurrencyData(29, 30, "ILS", "Israeli New Sheqel (ILS)", 1),
				new CurrencyData(30, 32, "JMD", "Jamaican Dollar (JMD)", 1),
				new CurrencyData(31, 34, "JPY", "Japanese Yen (JPY)", 1),
				new CurrencyData(32, 33, "JOD", "Jordanian Dinar (JOD)", 1),
				new CurrencyData(33, 39, "KZT", "Kazakhstani Tenge (KZT)", 1),
				new CurrencyData(34, 35, "KES", "Kenyan Shilling (KES)", 1),
				new CurrencyData(35, 37, "KWD", "Kuwaiti Dinar (KWD)", 1),
				new CurrencyData(36, 43, "LVL", "Latvian Lats (LVL)", 1),
				new CurrencyData(37, 40, "LBP", "Lebanese Pound (LBP)", 10),
				new CurrencyData(38, 42, "LTL", "Lithuanian Litas (LTL)", 1),
				new CurrencyData(39, 46, "MKD", "Macedonian Denar (MKD)", 1),
				new CurrencyData(40, 49, "MYR", "Malaysian Ringgit (MYR)", 1),
				new CurrencyData(41, 47, "MUR", "Mauritian Rupee (MUR)", 1),
				new CurrencyData(42, 48, "MXN", "Mexican Peso (MXN)", 1),
				new CurrencyData(43, 45, "MDL", "Moldovan Leu (MDL)", 1),
				new CurrencyData(44, 44, "MAD", "Moroccan Dirham (MAD)", 1),
				new CurrencyData(45, 50, "NAD", "Namibian Dollar (NAD)", 1),
				new CurrencyData(46, 54, "NPR", "Nepalese Rupee (NPR)", 1),
				new CurrencyData(47, 1, "ANG", "Netherlands Antillean Guilder (ANG)", 1),
				new CurrencyData(48, 78, "TWD", "New Taiwan Dollar (TWD)", 1),
				new CurrencyData(49, 55, "NZD", "New Zealand Dollar (NZD)", 1),
				new CurrencyData(50, 52, "NIO", "Nicaraguan Cordoba (NIO)", 1),
				new CurrencyData(51, 51, "NGN", "Nigerian Naira (NGN)", 1),
				new CurrencyData(52, 53, "NOK", "Norwegian Krone (NOK)", 1),
				new CurrencyData(53, 56, "OMR", "Omani Rial (OMR)", 1),
				new CurrencyData(54, 60, "PKR", "Pakistani Rupee (PKR)", 1),
				new CurrencyData(55, 58, "PGK", "Papua New Guinean Kina (PGK)", 1),
				new CurrencyData(56, 62, "PYG", "Paraguayan Guarani (PYG)", 10),
				new CurrencyData(57, 57, "PEN", "Peruvian Nuevo Sol (PEN)", 1),
				new CurrencyData(58, 59, "PHP", "Philippine Peso (PHP)", 1),
				new CurrencyData(59, 61, "PLN", "Polish Zloty (PLN)", 1),
				new CurrencyData(60, 63, "QAR", "Qatari Rial (QAR)", 1),
				new CurrencyData(61, 64, "RON", "Romanian Leu (RON)", 1),
				new CurrencyData(62, 66, "RUB", "Russian Ruble (RUB)", 1),
				new CurrencyData(63, 73, "SVC", "Salvadoran Colon (SVC)", 1),
				new CurrencyData(64, 67, "SAR", "Saudi Riyal (SAR)", 1),
				new CurrencyData(65, 65, "RSD", "Serbian Dinar (RSD)", 1),
				new CurrencyData(66, 68, "SCR", "Seychellois Rupee (SCR)", 1),
				new CurrencyData(67, 72, "SLL", "Sierra Leonean Leone (SLL)", 10),
				new CurrencyData(68, 70, "SGD", "Singapore Dollar (SGD)", 1),
				new CurrencyData(69, 71, "SKK", "Slovak Koruna (SKK)", 1),
				new CurrencyData(70, 88, "ZAR", "South African Rand (ZAR)", 1),
				new CurrencyData(71, 36, "KRW", "South Korean Won (KRW)", 10),
				new CurrencyData(72, 41, "LKR", "Sri Lankan Rupee (LKR)", 1),
				new CurrencyData(73, 69, "SEK", "Swedish Krona (SEK)", 1),
				new CurrencyData(74, 11, "CHF", "Swiss Franc (CHF)", 1),
				new CurrencyData(75, 79, "TZS", "Tanzanian Shilling (TZS)", 10),
				new CurrencyData(76, 74, "THB", "Thai Baht (THB)", 1),
				new CurrencyData(77, 77, "TTD", "Trinidad and Tobago Dollar (TTD)", 1),
				new CurrencyData(78, 75, "TND", "Tunisian Dinar (TND)", 1),
				new CurrencyData(79, 76, "TRY", "Turkish Lira (TRY)", 1),
				new CurrencyData(80, 81, "UGX", "Ugandan Shilling (UGX)", 10),
				new CurrencyData(81, 80, "UAH", "Ukrainian Hryvnia (UAH)", 1),
				new CurrencyData(82, 0, "AED", "United Arab Emirates Dirham (AED)", 1),
				new CurrencyData(83, 83, "UYU", "Uruguayan Peso (UYU)", 1),
				new CurrencyData(84, 82, "USD", "US Dollar (USD)", 1),
				new CurrencyData(85, 84, "UZS", "Uzbekistan Som (UZS)", 10),
				new CurrencyData(86, 85, "VEF", "Venezuelan Bolivar (VEF)", 1),
				new CurrencyData(87, 86, "VND", "Vietnamese Dong (VND)", 100),
				new CurrencyData(88, 87, "YER", "Yemeni Rial (YER)", 1),
				new CurrencyData(89, 89, "ZMK", "Zambian Kwacha (ZMK)", 10)
				
		};
		
		return currencyData;
	}
}
