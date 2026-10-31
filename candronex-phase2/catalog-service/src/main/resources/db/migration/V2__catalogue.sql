INSERT INTO service_specification (id, name) VALUES
  ('SPEC_URLLC', 'Slice URLLC - commande et contrôle'),
  ('SPEC_EMBB',  'Slice eMBB - transmission vidéo');

INSERT INTO service_offering (id, name, description, specification_id) VALUES
  ('C2_URLLC',     'Command and Control',  'Connectivité à faible latence pour le pilotage du drone', 'SPEC_URLLC'),
  ('IMAGERY_EMBB', 'Imagery Streaming',    'Connectivité à haut débit pour la transmission vidéo',    'SPEC_EMBB');

INSERT INTO service_characteristic (specification_id, name, value) VALUES
  ('SPEC_URLLC', 'sliceType', 'URLLC'),
  ('SPEC_URLLC', 'sst',       '2'),
  ('SPEC_URLLC', 'sd',        '000123'),
  ('SPEC_URLLC', 'dnn',       'telemetry.candronex.com'),
  ('SPEC_URLLC', '5qi',       '82'),
  ('SPEC_EMBB',  'sliceType', 'eMBB'),
  ('SPEC_EMBB',  'sst',       '1'),
  ('SPEC_EMBB',  'sd',        '000456'),
  ('SPEC_EMBB',  'dnn',       'video.candronex.com'),
  ('SPEC_EMBB',  '5qi',       '9');
