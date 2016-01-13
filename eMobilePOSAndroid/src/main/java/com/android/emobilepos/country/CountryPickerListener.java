package com.android.emobilepos.country;

/**
 * Inform the client which country has been selected
 *
 */
public interface CountryPickerListener {
	void onSelectCountry(String name, String code);
}
