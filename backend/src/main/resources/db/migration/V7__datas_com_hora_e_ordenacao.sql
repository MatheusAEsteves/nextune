-- Datas que ordenam listas (mais recentes primeiro) passam de DATE para DATETIME.
-- Antes, duas ações no mesmo dia não tinham ordem definida.
ALTER TABLE post MODIFY data_publicacao DATETIME NOT NULL;
ALTER TABLE interacao MODIFY data_criacao DATETIME NOT NULL;
ALTER TABLE interacao MODIFY data_atualizacao DATETIME NOT NULL;
ALTER TABLE preferencia_usuario MODIFY data_atualizacao DATETIME NOT NULL;
ALTER TABLE comentario MODIFY data_comentario DATETIME NOT NULL;
ALTER TABLE denuncia MODIFY data_denuncia DATETIME NOT NULL;
