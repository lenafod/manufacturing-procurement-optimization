package com.mpo.enums;

public enum SectionShape {
	ROUND("Okrugli"),
	RECTANGULAR("Pravougaoni"),
	HEXAGONAL("Šestougaoni"),
	PIPE("Cevasti"),
	CUBE("Kvadratni");

	private final String displayName;

	SectionShape(String displayName) {
		this.displayName = displayName;
	}

	// naziv za prikaz korisniku (PDF, front) - sam enum ostaje na engleskom
	// jer je vec upisan u bazu preko Liquibase-a (@Enumerated(EnumType.STRING))
	public String getDisplayName() {
		return displayName;
	}
}
