package fr.insalyon.pldagile.erreurs.exception;
/**
 * Exception levée lorsqu'un couple Pickup/Delivery ne respecte pas la contrainte de précédence.
 * <p>
 * Cette exception est une {@link RuntimeException} et peut être lancée sans être obligatoirement déclarée.
 * Elle signale un problème lors d'un ajout d'une livraison.
 */
public class ContrainteDePrecedenceException extends RuntimeException {
    public ContrainteDePrecedenceException(String message) {
        super(message);
    }
}
