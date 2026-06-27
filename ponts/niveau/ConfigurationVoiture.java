package ponts.niveau;

import java.io.Serializable;

/**
 * Paramètres physiques et compétences propres à un niveau.
 */
public class ConfigurationVoiture implements Serializable {

    private static final long serialVersionUID = 1L;

    public float masse = 1.0f;
    public float vitesseInitiale = 0f;
    public float vitesseMax = 16f;
    public float acceleration = 160f;

    public boolean boostAutorise = true;
    public float boostCooldown = 4f;
    public float boostDuree = 1.6f;
    public float boostIntensite = 2.2f;

    public boolean volAutorise = true;
    public float volCooldown = 6f;
    public float volDuree = 1.25f;
    public float volPousseeVerticale = 42f;
    public float volPousseeHorizontale = 18f;
    public float volHauteurMax = 14f;
    public boolean volSubitGravite = true;
    public boolean volDeclenchementManuel = true;
}
