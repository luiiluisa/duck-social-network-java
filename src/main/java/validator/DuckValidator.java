package validator;

import exceptions.ValidationException;
import model.Duck;
import model.Enums.TipDuck;

public class DuckValidator implements ValidationStrategy<Duck> {
    @Override
    public boolean validate(Duck d) {
        if (d == null) throw new ValidationException("Duck null");
        if (d.getTip() == null) throw new ValidationException("Tip lipsa");
        if (d.getTip() != TipDuck.SWIMMING && d.getTip() != TipDuck.FLYING && d.getTip() != TipDuck.FLYING_AND_SWIMMING)
            throw new ValidationException("Tip invalid");
        if (d.getViteza() <= 0)
            throw new ValidationException("Viteza trebuie sa fie >0");
        if (d.getRezistenta() <= 0)
            throw new ValidationException("Rezistenta trebuie sa fie > 0 ");
        return true;
    }
}
