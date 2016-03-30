package de.unidue.ltl.pos.trainmodel.morph;

public class Morph {

	private String item;
	private String wkl;

	private String typ;
	private String num;
	private String form;

	private String komp;

	private String particip;
	private String der;
	private String numericVal;

	public Morph() {
		this.item = "*";
		this.wkl = "*";
		this.form = "*";
		this.typ = "*";
		this.num = "*";
		this.komp = "*";
		this.particip = "*";
		this.der = "*";
		this.numericVal="*";
	}

	public void makeVerb(String item, String wkl, String form, String typ,
			String num) {
		this.item = item;
		this.wkl = wkl;
		this.form = form;
		this.typ = typ;
		this.num = num;
	}

	public void makeAdj(String item, String wkl, String komp) {
		this.item = item;
		this.wkl = wkl;
		this.komp = komp;
	}

	public void makeArt(String item, String wkl, String typ) {
		this.item = item;
		this.wkl = wkl;
		this.typ = typ;
	}

	public void makeAdv(String item, String wkl, String typ) {
		this.item = item;
		this.wkl = wkl;
		this.typ = typ;
	}

	public void makeParticip(String particip, String item, String wkl,
			String der, String num, String kmp) {
		this.particip = particip;
		this.item = item;
		this.wkl = wkl;
		this.der = der;
		this.num = num;
		this.komp = kmp;
	}

	public void makeSubstantiv(String item, String wkl, String der, String num) {
		this.item = item;
		this.wkl = wkl;
		this.der = der;
		this.num = num;
	}

	public void makePronomen(String item, String wkl, String typ, String num) {
		this.item = item;
		this.wkl = wkl;
		this.typ = typ;
		this.num = num;
	}
	
	public void makeZahl(String item, String wkl, String numVal) {
		this.item = item;
		this.wkl = wkl;
		this.numericVal = numVal;
	}


	public void makeDefault(String item, String wkl) {
		this.item = item;
		this.wkl = wkl;
	}

	public String getItem() {
		return item;
	}

	public String getWkl() {
		return wkl;
	}

	public String getForm() {
		return form;
	}

	public String getTyp() {
		return typ;
	}

	public String getNum() {
		return num;
	}

	public String getKomp() {
		return komp;
	}

	public String getDer() {
		return der;
	}

	public String getParticip() {
		return particip;
	}
	
	public String getNumericVal(){
		return numericVal;
	}

}
