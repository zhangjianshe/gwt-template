/*vw_project*/
CREATE VIEW "public"."vw_project" AS
SELECT
    p.*,
    myp.my_id,
    myp.permission

FROM dev_my_project as myp
left JOIN dev_project as p ON myp.project_id = p.id