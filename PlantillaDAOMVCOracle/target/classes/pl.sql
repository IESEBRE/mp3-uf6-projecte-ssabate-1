create or replace TRIGGER generate_alumnes_id_trg
BEFORE INSERT ON alumnes
FOR EACH ROW
BEGIN
SELECT NVL(MAX(id), 0) + 1 INTO :NEW.id FROM alumnes;
END;
/