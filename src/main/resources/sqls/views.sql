/*vw_project*/
CREATE VIEW vw_project AS
SELECT p.id,
       p.name,
       p.create_time,
       p.user_id,
       p.source_url,
       p.build_script,
       p.deploy_server,
       p.service_url,
       p.owner_name,
       p.owner_kind,
       p.summary,
       p.member_count,
       dpm.user_id AS my_id,
       dpm.permission,
       dpm.owner
FROM dev_project_member dpm
         LEFT JOIN dev_project p ON p.id::text = dpm.project_id::text

/*vw_project_member*/
CREATE VIEW vw_project_member AS
SELECT
    dev_project_member.user_id,
    dev_project_member.project_id,
    dev_project_member.permission,
    dev_project_member.create_time,
    dev_project_member.owner,
    rbac_user.user_name,
    rbac_user.nick_name,
    rbac_user.user_type,
    rbac_user.email,
    rbac_user.avatar,
    rbac_user.status
FROM
    dev_project_member
        INNER JOIN
    rbac_user
    ON
        dev_project_member.user_id = rbac_user.user_id